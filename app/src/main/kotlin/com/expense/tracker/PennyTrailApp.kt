package com.expense.tracker

import android.app.Application
import com.expense.tracker.data.local.AppDatabase
import com.expense.tracker.data.repository.CreditRepository
import com.expense.tracker.data.repository.ExpenseRepository
import com.expense.tracker.data.repository.ProductRepository
import com.expense.tracker.data.repository.SaleRepository
import com.expense.tracker.data.repository.StockRepository

class PennyTrailApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val saleRepository by lazy { SaleRepository(database.saleDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val creditRepository by lazy { CreditRepository(database.creditDao(), database.creditPaymentDao()) }
    val stockRepository by lazy { StockRepository(database.stockEntryDao(), database.productDao()) }
}
