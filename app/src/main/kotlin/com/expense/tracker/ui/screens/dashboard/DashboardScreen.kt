package com.expense.tracker.ui.screens.dashboard

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.ui.components.SummaryCard
import com.expense.tracker.ui.navigation.Screen
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils
import com.expense.tracker.util.CsvExporter
import com.expense.tracker.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(
            app.expenseRepository, app.saleRepository, app.creditRepository
        )
    )
    val state by viewModel.uiState.collectAsState()
    val recentSales by viewModel.recentSales.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PennyTrail") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val intent = CsvExporter.exportAllData(context)
                            context.startActivity(Intent.createChooser(intent, "Export Data"))
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Data")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Sales",
                    value = CurrencyUtils.formatAmount(state.todaySales),
                    backgroundColor = MaterialTheme.pennyTrailColors.cardGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Expenses",
                    value = CurrencyUtils.formatAmount(state.todayExpenses),
                    backgroundColor = MaterialTheme.pennyTrailColors.cardRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Profit/Loss",
                    value = CurrencyUtils.formatAmount(state.todayProfit),
                    backgroundColor = if (state.todayProfit >= 0) MaterialTheme.pennyTrailColors.cardGreen else MaterialTheme.pennyTrailColors.cardRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Credits Due",
                    value = CurrencyUtils.formatAmount(state.totalOutstandingCredit),
                    backgroundColor = MaterialTheme.pennyTrailColors.cardAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Sales",
                    value = CurrencyUtils.formatAmount(state.monthSales),
                    backgroundColor = MaterialTheme.pennyTrailColors.cardBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Expenses",
                    value = CurrencyUtils.formatAmount(state.monthExpenses),
                    backgroundColor = MaterialTheme.pennyTrailColors.cardBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SummaryCard(
                title = "Monthly Profit/Loss",
                value = CurrencyUtils.formatAmount(state.monthProfit),
                backgroundColor = if (state.monthProfit >= 0) MaterialTheme.pennyTrailColors.cardGreen else MaterialTheme.pennyTrailColors.cardRed
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Sales
            if (recentSales.isNotEmpty()) {
                Text(
                    text = "Recent Sales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        recentSales.take(5).forEachIndexed { index, sale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.AddEditSale.createRoute(sale.id))
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sale.productName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        DateUtils.formatShortDate(sale.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    CurrencyUtils.formatAmount(sale.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.pennyTrailColors.profitGreen
                                )
                            }
                            if (index < minOf(4, recentSales.size - 1)) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Expenses
            if (recentExpenses.isNotEmpty()) {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        recentExpenses.take(5).forEachIndexed { index, expense ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.AddEditExpense.createRoute(expense.id))
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        expense.description.ifBlank { expense.category },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${DateUtils.formatShortDate(expense.date)} - ${expense.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    CurrencyUtils.formatAmount(expense.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.pennyTrailColors.lossRed
                                )
                            }
                            if (index < minOf(4, recentExpenses.size - 1)) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
