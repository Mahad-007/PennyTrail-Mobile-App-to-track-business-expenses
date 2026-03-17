package com.expense.tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.tracker.util.CurrencyUtils

@Composable
fun AddPaymentDialog(
    personName: String,
    remainingAmount: Double,
    onConfirm: (amount: Double, note: String, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(remainingAmount.let {
        if (it == it.toLong().toDouble()) it.toLong().toString() else "%.2f".format(it)
    }) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
    val isValid = parsedAmount > 0.0 && parsedAmount <= remainingAmount + 0.005

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment - $personName") },
        text = {
            Column {
                Text(
                    text = "Remaining: ${CurrencyUtils.formatAmount(remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(
                    label = "Date",
                    dateMillis = date,
                    onDateSelected = { date = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                CurrencyTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Payment Amount"
                )
                if (parsedAmount > remainingAmount + 0.005) {
                    Text(
                        text = "Amount exceeds remaining balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val paymentAmount = amount.toDoubleOrNull()
                    if (paymentAmount != null && paymentAmount > 0.0) {
                        onConfirm(paymentAmount, note.trim(), date)
                    }
                },
                enabled = isValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
