package com.expense.tracker.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.ExpenseEntity
import com.expense.tracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseFormState(
    val date: Long = System.currentTimeMillis(),
    val amount: String = "",
    val category: String = "Other",
    val description: String = "",
    val isEditing: Boolean = false
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val expenses: StateFlow<List<ExpenseEntity>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    fun loadExpense(id: Long) {
        if (id == -1L) return
        viewModelScope.launch {
            repository.getExpenseById(id)?.let { expense ->
                _formState.value = ExpenseFormState(
                    date = expense.date,
                    amount = expense.amount.toString(),
                    category = expense.category,
                    description = expense.description,
                    isEditing = true
                )
            }
        }
    }

    fun updateDate(date: Long) { _formState.value = _formState.value.copy(date = date) }
    fun updateAmount(amount: String) { _formState.value = _formState.value.copy(amount = amount) }
    fun updateCategory(category: String) { _formState.value = _formState.value.copy(category = category) }
    fun updateDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }

    fun saveExpense(existingId: Long = -1L, onComplete: () -> Unit) {
        val state = _formState.value
        val amount = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            if (state.isEditing && existingId != -1L) {
                repository.update(
                    ExpenseEntity(
                        id = existingId,
                        date = state.date,
                        amount = amount,
                        category = state.category,
                        description = state.description.trim()
                    )
                )
            } else {
                repository.insert(
                    ExpenseEntity(
                        date = state.date,
                        amount = amount,
                        category = state.category,
                        description = state.description.trim()
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteExpense(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.getExpenseById(id)?.let {
                repository.delete(it)
            }
            onComplete()
        }
    }

    fun resetForm() { _formState.value = ExpenseFormState() }

    companion object {
        val CATEGORIES = listOf("Rent", "Supplies", "Utilities", "Transport", "Salary", "Food", "Other")

        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExpenseViewModel(repository) as T
                }
            }
    }
}
