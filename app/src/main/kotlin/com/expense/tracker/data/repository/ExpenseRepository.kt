package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.ExpenseDao
import com.expense.tracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    fun getExpensesByDate(startOfDay: Long, endOfDay: Long) = dao.getExpensesByDate(startOfDay, endOfDay)
    fun getTotalExpenseForDate(startOfDay: Long, endOfDay: Long) = dao.getTotalExpenseForDate(startOfDay, endOfDay)
    fun getTotalExpenseForRange(startDate: Long, endDate: Long) = dao.getTotalExpenseForRange(startDate, endDate)
    suspend fun getExpenseById(id: Long) = dao.getExpenseById(id)
    suspend fun insert(expense: ExpenseEntity) = dao.insert(expense)
    suspend fun update(expense: ExpenseEntity) = dao.update(expense)
    suspend fun delete(expense: ExpenseEntity) = dao.delete(expense)
}
