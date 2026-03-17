package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.CreditDao
import com.expense.tracker.data.local.dao.CreditPaymentDao
import com.expense.tracker.data.local.entity.CreditEntity
import com.expense.tracker.data.local.entity.CreditPaymentEntity
import kotlinx.coroutines.flow.Flow

class CreditRepository(
    private val dao: CreditDao,
    private val paymentDao: CreditPaymentDao
) {
    fun getAllCredits(): Flow<List<CreditEntity>> = dao.getAllCredits()
    fun getUnpaidCredits(): Flow<List<CreditEntity>> = dao.getUnpaidCredits()
    fun getTotalOutstandingCredit(): Flow<Double> = dao.getTotalOutstandingCredit()
    suspend fun getCreditById(id: Long) = dao.getCreditById(id)
    suspend fun insert(credit: CreditEntity) = dao.insert(credit)
    suspend fun update(credit: CreditEntity) = dao.update(credit)
    suspend fun markAsPaid(id: Long, paidDate: Long) = dao.markAsPaid(id, paidDate)
    suspend fun delete(credit: CreditEntity) = dao.delete(credit)

    fun getPaymentsForCredit(creditId: Long): Flow<List<CreditPaymentEntity>> =
        paymentDao.getPaymentsForCredit(creditId)

    suspend fun addPayment(creditId: Long, amount: Double, note: String = "", date: Long = System.currentTimeMillis()) {
        paymentDao.insert(
            CreditPaymentEntity(
                creditId = creditId,
                amount = amount,
                note = note,
                date = date
            )
        )
        dao.addPayment(creditId, amount)

        // Auto-mark as fully paid when remaining balance is zero or less
        // Note: getCreditById returns the already-updated amountPaid
        val credit = dao.getCreditById(creditId)
        if (credit != null && credit.amountPaid >= credit.amount - 0.005) {
            dao.markAsPaid(creditId, date)
        }
    }
}
