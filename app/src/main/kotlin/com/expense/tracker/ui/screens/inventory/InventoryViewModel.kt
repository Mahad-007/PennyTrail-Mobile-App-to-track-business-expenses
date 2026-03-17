package com.expense.tracker.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.ProductEntity
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

data class InventoryItem(
    val product: ProductEntity,
    val totalStock: Int,
    val totalSold: Int,
    val availableStock: Int,
    val lastPurchasePrice: Double,
    val sellingPrice: Double,
    val inventoryValue: Double
)

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _totalInventoryValue = MutableStateFlow(0.0)
    val totalInventoryValue: StateFlow<Double> = _totalInventoryValue.asStateFlow()

    private val _stockDialogProduct = MutableStateFlow<ProductEntity?>(null)
    val stockDialogProduct: StateFlow<ProductEntity?> = _stockDialogProduct.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                productRepository.getActiveProducts(),
                stockRepository.getAllEntries()
            ) { products, allEntries ->
                products.map { product ->
                    val productEntries = allEntries.filter { it.productId == product.id }
                    val latestEntry = productEntries.firstOrNull() // already sorted by date DESC
                    val totalSold = saleRepository.getTotalQuantitySoldForProductSync(product.id)
                    val availableStock = product.stockQuantity - totalSold

                    InventoryItem(
                        product = product,
                        totalStock = product.stockQuantity,
                        totalSold = totalSold,
                        availableStock = availableStock,
                        lastPurchasePrice = latestEntry?.purchasePrice ?: 0.0,
                        sellingPrice = product.defaultPrice,
                        inventoryValue = availableStock * (latestEntry?.purchasePrice ?: 0.0)
                    )
                }
            }.collect { items ->
                _inventoryItems.value = items
                _totalInventoryValue.value = items.sumOf { it.inventoryValue }
            }
        }
    }

    fun showAddStockDialog(product: ProductEntity) {
        _stockDialogProduct.value = product
    }

    fun dismissAddStockDialog() {
        _stockDialogProduct.value = null
    }

    fun addStock(productId: Long, quantity: Int, purchasePrice: Double, note: String = "", date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            stockRepository.addStock(productId, quantity, purchasePrice, note, date)
            dismissAddStockDialog()
        }
    }

    companion object {
        fun factory(
            productRepository: ProductRepository,
            stockRepository: StockRepository,
            saleRepository: SaleRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InventoryViewModel(productRepository, stockRepository, saleRepository) as T
                }
            }
    }
}
