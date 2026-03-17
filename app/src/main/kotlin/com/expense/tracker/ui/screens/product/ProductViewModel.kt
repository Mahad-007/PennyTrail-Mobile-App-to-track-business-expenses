package com.expense.tracker.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.ProductEntity
import com.expense.tracker.data.local.entity.StockEntryEntity
import com.expense.tracker.data.repository.ProductRepository
import com.expense.tracker.data.repository.SaleRepository
import com.expense.tracker.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductFormState(
    val name: String = "",
    val defaultPrice: String = "",
    val description: String = "",
    val isEditing: Boolean = false
)

data class ProductWithStock(
    val product: ProductEntity,
    val totalStock: Int,
    val totalSold: Int,
    val availableStock: Int
)

class ProductViewModel(
    private val repository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _productsWithStock = MutableStateFlow<List<ProductWithStock>>(emptyList())
    val productsWithStock: StateFlow<List<ProductWithStock>> = _productsWithStock.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllProducts().collect { productList ->
                val withStock = productList.map { product ->
                    val totalSold = saleRepository.getTotalQuantitySoldForProductSync(product.id)
                    ProductWithStock(
                        product = product,
                        totalStock = product.stockQuantity,
                        totalSold = totalSold,
                        availableStock = product.stockQuantity - totalSold
                    )
                }
                _productsWithStock.value = withStock
            }
        }
    }

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    fun loadProduct(id: Long) {
        if (id == -1L) return
        viewModelScope.launch {
            repository.getProductById(id)?.let { product ->
                _formState.value = ProductFormState(
                    name = product.name,
                    defaultPrice = product.defaultPrice.toString(),
                    description = product.description,
                    isEditing = true
                )
            }
        }
    }

    fun updateName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updatePrice(price: String) { _formState.value = _formState.value.copy(defaultPrice = price) }
    fun updateDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }

    fun saveProduct(existingId: Long = -1L, onComplete: () -> Unit) {
        val state = _formState.value
        val price = state.defaultPrice.toDoubleOrNull() ?: return
        if (state.name.isBlank()) return

        viewModelScope.launch {
            if (state.isEditing && existingId != -1L) {
                val existing = repository.getProductById(existingId)
                repository.update(
                    ProductEntity(
                        id = existingId,
                        name = state.name.trim(),
                        defaultPrice = price,
                        description = state.description.trim(),
                        stockQuantity = existing?.stockQuantity ?: 0
                    )
                )
            } else {
                repository.insert(
                    ProductEntity(
                        name = state.name.trim(),
                        defaultPrice = price,
                        description = state.description.trim()
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteProduct(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.softDelete(id)
            onComplete()
        }
    }

    private val _stockEntries = MutableStateFlow<List<StockEntryEntity>>(emptyList())
    val stockEntries: StateFlow<List<StockEntryEntity>> = _stockEntries.asStateFlow()

    fun loadStockEntries(productId: Long) {
        viewModelScope.launch {
            stockRepository.getEntriesForProduct(productId).collect { entries ->
                _stockEntries.value = entries
            }
        }
    }

    fun addStock(productId: Long, quantity: Int, purchasePrice: Double = 0.0, note: String = "", date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            stockRepository.addStock(productId, quantity, purchasePrice, note, date)
        }
    }

    fun resetForm() { _formState.value = ProductFormState() }

    companion object {
        fun factory(
            repository: ProductRepository,
            saleRepository: SaleRepository,
            stockRepository: StockRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductViewModel(repository, saleRepository, stockRepository) as T
                }
            }
    }
}
