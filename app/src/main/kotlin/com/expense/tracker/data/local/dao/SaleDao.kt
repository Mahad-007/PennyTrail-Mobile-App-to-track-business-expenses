package com.expense.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.expense.tracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC")
    fun getSalesByDate(startOfDay: Long, endOfDay: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE date BETWEEN :startOfDay AND :endOfDay")
    fun getTotalSalesForDate(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalSalesForRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE productId = :productId")
    fun getTotalQuantitySoldForProduct(productId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE productId = :productId")
    suspend fun getTotalQuantitySoldForProductSync(productId: Long): Int

    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Update
    suspend fun update(sale: SaleEntity)

    @Delete
    suspend fun delete(sale: SaleEntity)
}
