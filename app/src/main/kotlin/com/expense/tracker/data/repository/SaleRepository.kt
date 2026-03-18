package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.CreditDao
import com.expense.tracker.data.local.dao.SaleDao
import com.expense.tracker.data.local.entity.CreditEntity
import com.expense.tracker.data.local.entity.PaymentType
import com.expense.tracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

class SaleRepository(
    private val dao: SaleDao,
    private val creditDao: CreditDao
) {
    fun getAllSales(): Flow<List<SaleEntity>> = dao.getAllSales()
    fun getSalesByDate(startOfDay: Long, endOfDay: Long) = dao.getSalesByDate(startOfDay, endOfDay)
    fun getTotalSalesForDate(startOfDay: Long, endOfDay: Long) = dao.getTotalSalesForDate(startOfDay, endOfDay)
    fun getTotalSalesForRange(startDate: Long, endDate: Long) = dao.getTotalSalesForRange(startDate, endDate)
    fun getTotalQuantitySoldForProduct(productId: Long) = dao.getTotalQuantitySoldForProduct(productId)
    suspend fun getTotalQuantitySoldForProductSync(productId: Long) = dao.getTotalQuantitySoldForProductSync(productId)
    suspend fun getSaleById(id: Long) = dao.getSaleById(id)

    suspend fun insert(sale: SaleEntity): Long {
        val saleId = dao.insert(sale)
        if (sale.paymentType == PaymentType.CREDIT.name) {
            creditDao.insert(
                CreditEntity(
                    personName = sale.customerName,
                    amount = sale.totalAmount,
                    description = "Sale: ${sale.productName} (${sale.quantity} x ${sale.unitPrice})",
                    date = sale.date,
                    linkedSaleId = saleId
                )
            )
        }
        return saleId
    }

    suspend fun update(sale: SaleEntity, previousPaymentType: String) {
        dao.update(sale)
        when {
            previousPaymentType == PaymentType.CASH.name && sale.paymentType == PaymentType.CREDIT.name -> {
                creditDao.insert(
                    CreditEntity(
                        personName = sale.customerName,
                        amount = sale.totalAmount,
                        description = "Sale: ${sale.productName} (${sale.quantity} x ${sale.unitPrice})",
                        date = sale.date,
                        linkedSaleId = sale.id
                    )
                )
            }
            previousPaymentType == PaymentType.CREDIT.name && sale.paymentType == PaymentType.CASH.name -> {
                creditDao.deleteCreditByLinkedSaleId(sale.id)
            }
            previousPaymentType == PaymentType.CREDIT.name && sale.paymentType == PaymentType.CREDIT.name -> {
                val linkedCredit = creditDao.getCreditByLinkedSaleId(sale.id)
                if (linkedCredit != null && linkedCredit.amountPaid == 0.0) {
                    creditDao.update(
                        linkedCredit.copy(
                            personName = sale.customerName,
                            amount = sale.totalAmount,
                            description = "Sale: ${sale.productName} (${sale.quantity} x ${sale.unitPrice})"
                        )
                    )
                }
            }
        }
    }

    suspend fun delete(sale: SaleEntity) {
        if (sale.paymentType == PaymentType.CREDIT.name) {
            creditDao.deleteCreditByLinkedSaleId(sale.id)
        }
        dao.delete(sale)
    }
}
