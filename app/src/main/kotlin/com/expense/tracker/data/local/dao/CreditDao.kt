package com.expense.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.expense.tracker.data.local.entity.CreditEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {

    @Query("SELECT * FROM credits ORDER BY isPaid ASC, date DESC")
    fun getAllCredits(): Flow<List<CreditEntity>>

    @Query("SELECT * FROM credits WHERE isPaid = 0 ORDER BY date DESC")
    fun getUnpaidCredits(): Flow<List<CreditEntity>>

    @Query("SELECT COALESCE(SUM(amount - amountPaid), 0.0) FROM credits WHERE isPaid = 0")
    fun getTotalOutstandingCredit(): Flow<Double>

    @Query("UPDATE credits SET amountPaid = amountPaid + :paymentAmount WHERE id = :creditId")
    suspend fun addPayment(creditId: Long, paymentAmount: Double)

    @Query("SELECT * FROM credits WHERE id = :id")
    suspend fun getCreditById(id: Long): CreditEntity?

    @Insert
    suspend fun insert(credit: CreditEntity): Long

    @Update
    suspend fun update(credit: CreditEntity)

    @Query("UPDATE credits SET isPaid = 1, paidDate = :paidDate WHERE id = :id")
    suspend fun markAsPaid(id: Long, paidDate: Long)

    @Query("SELECT * FROM credits WHERE linkedSaleId = :saleId LIMIT 1")
    suspend fun getCreditByLinkedSaleId(saleId: Long): CreditEntity?

    @Query("DELETE FROM credits WHERE linkedSaleId = :saleId")
    suspend fun deleteCreditByLinkedSaleId(saleId: Long)

    @Delete
    suspend fun delete(credit: CreditEntity)
}
