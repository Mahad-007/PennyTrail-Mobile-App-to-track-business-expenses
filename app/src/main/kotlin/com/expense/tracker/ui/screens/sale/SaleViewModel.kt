package com.expense.tracker.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.DailySalesSummary
import com.expense.tracker.data.local.entity.PaymentType
import com.expense.tracker.data.local.entity.ProductEntity
import com.expense.tracker.data.local.entity.SaleEntity
import com.expense.tracker.data.local.entity.SalesByProduct
import com.expense.tracker.data.repository.ProductRepository
import com.expense.tracker.data.repository.SaleRepository
import com.expense.tracker.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SaleFormState(
    val date: Long = System.currentTimeMillis(),
    val selectedProduct: ProductEntity? = null,
    val productName: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val paymentType: PaymentType = PaymentType.CASH,
    val customerName: String = "",
    val originalPaymentType: PaymentType = PaymentType.CASH,
    val isEditing: Boolean = false
) {
    val totalAmount: Double
        get() {
            val qty = quantity.toIntOrNull() ?: 0
            val price = unitPrice.toDoubleOrNull() ?: 0.0
            return qty * price
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModel(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val dailySales: StateFlow<List<DailySalesSummary>> = _selectedDate.flatMapLatest { date ->
        val start = DateUtils.startOfDay(date)
        val end = DateUtils.endOfDay(date)
        saleRepository.getSalesByDate(start, end).map { salesList ->
            if (salesList.isEmpty()) {
                emptyList()
            } else {
                val productBreakdown = salesList
                    .groupBy { it.productName }
                    .map { (name, productSales) ->
                        SalesByProduct(
                            productName = name,
                            totalQuantity = productSales.sumOf { it.quantity },
                            totalRevenue = productSales.sumOf { it.totalAmount },
                            sales = productSales.sortedByDescending { it.date }
                        )
                    }
                    .sortedByDescending { it.totalRevenue }
                listOf(
                    DailySalesSummary(
                        dateMillis = DateUtils.toEpochMillis(date),
                        productBreakdown = productBreakdown,
                        dailyTotal = salesList.sumOf { it.totalAmount }
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableProducts: StateFlow<List<ProductEntity>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(SaleFormState())
    val formState: StateFlow<SaleFormState> = _formState.asStateFlow()

    fun goToPreviousDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }
    fun goToNextDay() {
        if (_selectedDate.value.isBefore(LocalDate.now())) {
            _selectedDate.value = _selectedDate.value.plusDays(1)
        }
    }
    fun goToToday() { _selectedDate.value = LocalDate.now() }
    fun goToDate(date: LocalDate) { _selectedDate.value = date }

    fun loadSale(id: Long) {
        if (id == -1L) return
        viewModelScope.launch {
            saleRepository.getSaleById(id)?.let { sale ->
                val type = try { PaymentType.valueOf(sale.paymentType) } catch (_: Exception) { PaymentType.CASH }
                _formState.value = SaleFormState(
                    date = sale.date,
                    productName = sale.productName,
                    quantity = sale.quantity.toString(),
                    unitPrice = sale.unitPrice.toString(),
                    paymentType = type,
                    customerName = sale.customerName,
                    originalPaymentType = type,
                    isEditing = true
                )
            }
        }
    }

    fun updateDate(date: Long) { _formState.value = _formState.value.copy(date = date) }
    fun updateQuantity(qty: String) { _formState.value = _formState.value.copy(quantity = qty) }
    fun updateUnitPrice(price: String) { _formState.value = _formState.value.copy(unitPrice = price) }
    fun updateProductName(name: String) { _formState.value = _formState.value.copy(productName = name) }
    fun updatePaymentType(type: PaymentType) { _formState.value = _formState.value.copy(paymentType = type) }
    fun updateCustomerName(name: String) { _formState.value = _formState.value.copy(customerName = name) }

    fun selectProduct(product: ProductEntity) {
        _formState.value = _formState.value.copy(
            selectedProduct = product,
            productName = product.name,
            unitPrice = product.defaultPrice.toString()
        )
    }

    fun saveSale(existingId: Long = -1L, onComplete: () -> Unit) {
        val state = _formState.value
        val qty = state.quantity.toIntOrNull() ?: return
        val price = state.unitPrice.toDoubleOrNull() ?: return
        if (state.productName.isBlank()) return
        if (state.paymentType == PaymentType.CREDIT && state.customerName.isBlank()) return

        val sale = SaleEntity(
            id = if (state.isEditing && existingId != -1L) existingId else 0,
            date = state.date,
            productId = state.selectedProduct?.id,
            productName = state.productName.trim(),
            quantity = qty,
            unitPrice = price,
            totalAmount = qty * price,
            paymentType = state.paymentType.name,
            customerName = if (state.paymentType == PaymentType.CREDIT) state.customerName.trim() else ""
        )

        viewModelScope.launch {
            if (state.isEditing && existingId != -1L) {
                saleRepository.update(sale, state.originalPaymentType.name)
            } else {
                saleRepository.insert(sale)
            }
            onComplete()
        }
    }

    fun deleteSale(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            saleRepository.getSaleById(id)?.let {
                saleRepository.delete(it)
            }
            onComplete()
        }
    }

    fun resetForm() { _formState.value = SaleFormState() }

    companion object {
        fun factory(saleRepository: SaleRepository, productRepository: ProductRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SaleViewModel(saleRepository, productRepository) as T
                }
            }
    }
}
