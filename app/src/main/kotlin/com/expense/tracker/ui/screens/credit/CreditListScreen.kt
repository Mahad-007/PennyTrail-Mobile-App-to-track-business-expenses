package com.expense.tracker.ui.screens.credit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.data.local.entity.CreditEntity
import com.expense.tracker.ui.components.AddPaymentDialog
import com.expense.tracker.ui.components.EmptyStateMessage
import com.expense.tracker.ui.navigation.Screen
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils
import com.expense.tracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditListScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: CreditViewModel = viewModel(factory = CreditViewModel.factory(app.creditRepository))
    val allCredits by viewModel.credits.collectAsState()
    val unpaidCredits by viewModel.unpaidCredits.collectAsState()
    val showUnpaidOnly by viewModel.showUnpaidOnly.collectAsState()
    val totalOutstanding by viewModel.totalOutstanding.collectAsState()

    var paymentDialogCredit by remember { mutableStateOf<CreditEntity?>(null) }

    val displayCredits = if (showUnpaidOnly) unpaidCredits else allCredits

    if (paymentDialogCredit != null) {
        val credit = paymentDialogCredit!!
        val remaining = credit.amount - credit.amountPaid
        AddPaymentDialog(
            personName = credit.personName,
            remainingAmount = remaining,
            onConfirm = { amount, note, date ->
                viewModel.addPayment(credit.id, amount, note, date)
                paymentDialogCredit = null
            },
            onDismiss = { paymentDialogCredit = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Credits")
                        Text(
                            text = "Outstanding: ${CurrencyUtils.formatAmount(totalOutstanding)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.pennyTrailColors.creditAmber
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilter() }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = if (showUnpaidOnly) "Show All" else "Show Unpaid Only"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditCredit.createRoute())
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Credit")
            }
        }
    ) { padding ->
        if (displayCredits.isEmpty()) {
            EmptyStateMessage(
                if (showUnpaidOnly) "No unpaid credits." else "No credits yet. Tap + to add one.",
                Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayCredits, key = { it.id }) { credit ->
                    val remaining = credit.amount - credit.amountPaid
                    val progress = if (credit.amount > 0) (credit.amountPaid / credit.amount).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.AddEditCredit.createRoute(credit.id))
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (credit.isPaid)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = credit.personName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (credit.isPaid) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = DateUtils.formatShortDate(credit.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (credit.description.isNotBlank()) {
                                        Text(
                                            text = credit.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (credit.linkedSaleId != null) {
                                        Text(
                                            text = "From Sale",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.pennyTrailColors.creditAmber
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatAmount(credit.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (credit.isPaid) MaterialTheme.pennyTrailColors.profitGreen else MaterialTheme.pennyTrailColors.creditAmber
                                    )
                                    if (!credit.isPaid) {
                                        IconButton(onClick = { paymentDialogCredit = credit }) {
                                            Icon(
                                                Icons.Default.Payment,
                                                contentDescription = "Add Payment",
                                                tint = MaterialTheme.pennyTrailColors.profitGreen
                                            )
                                        }
                                    }
                                }
                            }

                            if (!credit.isPaid) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.pennyTrailColors.profitGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Paid: ${CurrencyUtils.formatAmount(credit.amountPaid)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.pennyTrailColors.profitGreen
                                    )
                                    Text(
                                        text = "Remaining: ${CurrencyUtils.formatAmount(remaining)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.pennyTrailColors.creditAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
