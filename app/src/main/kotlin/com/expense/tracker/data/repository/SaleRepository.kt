package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.SaleDao
import com.expense.tracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

class SaleRepository(private val dao: SaleDao) {
    fun getAllSales(): Flow<List<SaleEntity>> = dao.getAllSales()
    fun getSalesByDate(startOfDay: Long, endOfDay: Long) = dao.getSalesByDate(startOfDay, endOfDay)
    fun getTotalSalesForDate(startOfDay: Long, endOfDay: Long) = dao.getTotalSalesForDate(startOfDay, endOfDay)
    fun getTotalSalesForRange(startDate: Long, endDate: Long) = dao.getTotalSalesForRange(startDate, endDate)
    fun getTotalQuantitySoldForProduct(productId: Long) = dao.getTotalQuantitySoldForProduct(productId)
    suspend fun getTotalQuantitySoldForProductSync(productId: Long) = dao.getTotalQuantitySoldForProductSync(productId)
    suspend fun getSaleById(id: Long) = dao.getSaleById(id)
    suspend fun insert(sale: SaleEntity) = dao.insert(sale)
    suspend fun update(sale: SaleEntity) = dao.update(sale)
    suspend fun delete(sale: SaleEntity) = dao.delete(sale)
}
