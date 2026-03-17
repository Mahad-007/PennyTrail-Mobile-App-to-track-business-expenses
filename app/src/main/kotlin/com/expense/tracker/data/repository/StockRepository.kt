package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.ProductDao
import com.expense.tracker.data.local.dao.StockEntryDao
import com.expense.tracker.data.local.entity.StockEntryEntity
import kotlinx.coroutines.flow.Flow

class StockRepository(
    private val stockEntryDao: StockEntryDao,
    private val productDao: ProductDao
) {
    fun getEntriesForProduct(productId: Long): Flow<List<StockEntryEntity>> =
        stockEntryDao.getEntriesForProduct(productId)

    fun getAllEntries(): Flow<List<StockEntryEntity>> =
        stockEntryDao.getAllEntries()

    suspend fun getLatestEntryForProduct(productId: Long): StockEntryEntity? =
        stockEntryDao.getLatestEntryForProduct(productId)

    suspend fun addStock(
        productId: Long,
        quantity: Int,
        purchasePrice: Double = 0.0,
        note: String = "",
        date: Long = System.currentTimeMillis()
    ) {
        stockEntryDao.insert(
            StockEntryEntity(
                productId = productId,
                quantity = quantity,
                purchasePrice = purchasePrice,
                note = note,
                date = date
            )
        )
        productDao.addStock(productId, quantity)
    }
}
