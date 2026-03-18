package com.expense.tracker.ui.screens.credit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.ui.components.AddPaymentDialog
import com.expense.tracker.ui.components.ConfirmDeleteDialog
import com.expense.tracker.ui.components.CurrencyTextField
import com.expense.tracker.ui.components.DatePickerField
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils
import com.expense.tracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCreditScreen(creditId: Long, navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: CreditViewModel = viewModel(factory = CreditViewModel.factory(app.creditRepository))
    val formState by viewModel.formState.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val isEditing = creditId != -1L
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(creditId) {
        if (isEditing) viewModel.loadCredit(creditId)
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteCredit(creditId) { navController.popBackStack() }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPaymentDialog && isEditing) {
        val totalAmount = formState.amount.toDoubleOrNull() ?: 0.0
        val remaining = totalAmount - formState.amountPaid
        AddPaymentDialog(
            personName = formState.personName,
            remainingAmount = remaining,
            onConfirm = { amount, note, date ->
                viewModel.addPayment(creditId, amount, note, date)
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Credit" else "Add Credit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Payment summary card (only when editing and has payments or partial payment)
            if (isEditing && formState.amountPaid > 0) {
                val totalAmount = formState.amount.toDoubleOrNull() ?: 0.0
                val remaining = totalAmount - formState.amountPaid
                val progress = if (totalAmount > 0) (formState.amountPaid / totalAmount).toFloat().coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyUtils.formatAmount(totalAmount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Paid", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyUtils.formatAmount(formState.amountPaid),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.pennyTrailColors.profitGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyUtils.formatAmount(remaining),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0) MaterialTheme.pennyTrailColors.creditAmber else MaterialTheme.pennyTrailColors.profitGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.pennyTrailColors.profitGreen,
                            trackColor = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = formState.personName,
                onValueChange = { viewModel.updatePersonName(it) },
                label = { Text("Person Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (formState.linkedSaleId != null) {
                OutlinedTextField(
                    value = "Rs. ${formState.amount}",
                    onValueChange = {},
                    label = { Text("Amount") },
                    enabled = false,
                    supportingText = { Text("Amount linked to sale. Edit the sale to change.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CurrencyTextField(
                    label = "Amount",
                    value = formState.amount,
                    onValueChange = { viewModel.updateAmount(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DatePickerField(
                label = "Date",
                dateMillis = formState.date,
                onDateSelected = { viewModel.updateDate(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (optional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveCredit(creditId) { navController.popBackStack() } },
                enabled = formState.personName.isNotBlank() && formState.amount.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Credit")
            }

            // Payment history section (only when editing)
            if (isEditing && !formState.isPaid) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddBox, contentDescription = null)
                    Text("  Add Payment", fontWeight = FontWeight.Medium)
                }
            }

            if (isEditing && payments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        payments.forEach { payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = DateUtils.formatDate(payment.date),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (payment.note.isNotBlank()) {
                                        Text(
                                            text = payment.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyUtils.formatAmount(payment.amount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.pennyTrailColors.profitGreen
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
