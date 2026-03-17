package com.expense.tracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.ExpenseEntity
import com.expense.tracker.data.local.entity.SaleEntity
import com.expense.tracker.data.repository.CreditRepository
import com.expense.tracker.data.repository.ExpenseRepository
import com.expense.tracker.data.repository.SaleRepository
import com.expense.tracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val todaySales: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayProfit: Double = 0.0,
    val monthSales: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val monthProfit: Double = 0.0,
    val totalOutstandingCredit: Double = 0.0,
    val recentSales: List<SaleEntity> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList()
)

class DashboardViewModel(
    expenseRepository: ExpenseRepository,
    saleRepository: SaleRepository,
    creditRepository: CreditRepository
) : ViewModel() {

    private val todayStart = DateUtils.startOfDay()
    private val todayEnd = DateUtils.endOfDay()
    private val monthStart = DateUtils.startOfMonth()
    private val monthEnd = DateUtils.endOfMonth()

    val uiState: StateFlow<DashboardUiState> = combine(
        saleRepository.getTotalSalesForDate(todayStart, todayEnd),
        expenseRepository.getTotalExpenseForDate(todayStart, todayEnd),
        saleRepository.getTotalSalesForRange(monthStart, monthEnd),
        expenseRepository.getTotalExpenseForRange(monthStart, monthEnd),
        creditRepository.getTotalOutstandingCredit()
    ) { todaySales, todayExpenses, monthSales, monthExpenses, outstandingCredit ->
        DashboardUiState(
            todaySales = todaySales,
            todayExpenses = todayExpenses,
            todayProfit = todaySales - todayExpenses,
            monthSales = monthSales,
            monthExpenses = monthExpenses,
            monthProfit = monthSales - monthExpenses,
            totalOutstandingCredit = outstandingCredit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    val recentSales: StateFlow<List<SaleEntity>> = saleRepository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentExpenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(
            expenseRepository: ExpenseRepository,
            saleRepository: SaleRepository,
            creditRepository: CreditRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(expenseRepository, saleRepository, creditRepository) as T
                }
            }
    }
}
