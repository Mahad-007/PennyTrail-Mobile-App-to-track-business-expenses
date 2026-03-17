package com.expense.tracker.ui.screens.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expense.tracker.data.local.entity.CreditEntity
import com.expense.tracker.data.local.entity.CreditPaymentEntity
import com.expense.tracker.data.repository.CreditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreditFormState(
    val personName: String = "",
    val amount: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val amountPaid: Double = 0.0,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val isEditing: Boolean = false
)

class CreditViewModel(private val repository: CreditRepository) : ViewModel() {

    val credits: StateFlow<List<CreditEntity>> = repository.getAllCredits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unpaidCredits: StateFlow<List<CreditEntity>> = repository.getUnpaidCredits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstanding: StateFlow<Double> = repository.getTotalOutstandingCredit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _formState = MutableStateFlow(CreditFormState())
    val formState: StateFlow<CreditFormState> = _formState.asStateFlow()

    private val _showUnpaidOnly = MutableStateFlow(true)
    val showUnpaidOnly: StateFlow<Boolean> = _showUnpaidOnly.asStateFlow()

    fun toggleFilter() { _showUnpaidOnly.value = !_showUnpaidOnly.value }

    private val _payments = MutableStateFlow<List<CreditPaymentEntity>>(emptyList())
    val payments: StateFlow<List<CreditPaymentEntity>> = _payments.asStateFlow()

    fun loadCredit(id: Long) {
        if (id == -1L) return
        viewModelScope.launch {
            repository.getCreditById(id)?.let { credit ->
                _formState.value = CreditFormState(
                    personName = credit.personName,
                    amount = credit.amount.toString(),
                    description = credit.description,
                    date = credit.date,
                    amountPaid = credit.amountPaid,
                    isPaid = credit.isPaid,
                    paidDate = credit.paidDate,
                    isEditing = true
                )
            }
            repository.getPaymentsForCredit(id).collect { paymentList ->
                _payments.value = paymentList
            }
        }
    }

    fun updatePersonName(name: String) { _formState.value = _formState.value.copy(personName = name) }
    fun updateAmount(amount: String) { _formState.value = _formState.value.copy(amount = amount) }
    fun updateDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }
    fun updateDate(date: Long) { _formState.value = _formState.value.copy(date = date) }

    fun saveCredit(existingId: Long = -1L, onComplete: () -> Unit) {
        val state = _formState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        if (state.personName.isBlank()) return

        viewModelScope.launch {
            if (state.isEditing && existingId != -1L) {
                repository.update(
                    CreditEntity(
                        id = existingId,
                        personName = state.personName.trim(),
                        amount = amount,
                        description = state.description.trim(),
                        date = state.date,
                        amountPaid = state.amountPaid,
                        isPaid = state.isPaid,
                        paidDate = state.paidDate
                    )
                )
            } else {
                repository.insert(
                    CreditEntity(
                        personName = state.personName.trim(),
                        amount = amount,
                        description = state.description.trim(),
                        date = state.date
                    )
                )
            }
            onComplete()
        }
    }

    fun markAsPaid(id: Long) {
        viewModelScope.launch {
            repository.markAsPaid(id, System.currentTimeMillis())
        }
    }

    fun addPayment(creditId: Long, amount: Double, note: String = "", date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.addPayment(creditId, amount, note, date)
        }
    }

    fun deleteCredit(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.getCreditById(id)?.let {
                repository.delete(it)
            }
            onComplete()
        }
    }

    fun resetForm() { _formState.value = CreditFormState() }

    companion object {
        fun factory(repository: CreditRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CreditViewModel(repository) as T
                }
            }
    }
}
