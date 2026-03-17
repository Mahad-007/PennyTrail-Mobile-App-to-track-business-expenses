package com.expense.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expense.tracker.data.local.entity.CreditPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditPaymentDao {

    @Query("SELECT * FROM credit_payments WHERE creditId = :creditId ORDER BY date DESC")
    fun getPaymentsForCredit(creditId: Long): Flow<List<CreditPaymentEntity>>

    @Insert
    suspend fun insert(payment: CreditPaymentEntity): Long
}
