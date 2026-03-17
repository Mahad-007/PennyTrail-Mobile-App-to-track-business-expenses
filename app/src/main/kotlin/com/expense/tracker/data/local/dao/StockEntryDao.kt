package com.expense.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expense.tracker.data.local.entity.StockEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockEntryDao {

    @Query("SELECT * FROM stock_entries WHERE productId = :productId ORDER BY date DESC")
    fun getEntriesForProduct(productId: Long): Flow<List<StockEntryEntity>>

    @Query("SELECT * FROM stock_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<StockEntryEntity>>

    @Query("SELECT * FROM stock_entries WHERE productId = :productId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestEntryForProduct(productId: Long): StockEntryEntity?

    @Insert
    suspend fun insert(entry: StockEntryEntity): Long
}
