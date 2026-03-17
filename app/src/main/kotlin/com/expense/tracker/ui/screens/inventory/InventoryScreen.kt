package com.expense.tracker.ui.screens.inventory

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
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.ui.components.AddStockDialog
import com.expense.tracker.ui.components.EmptyStateMessage
import com.expense.tracker.ui.components.SummaryCard
import com.expense.tracker.ui.navigation.Screen
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.factory(app.productRepository, app.stockRepository, app.saleRepository)
    )
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val totalValue by viewModel.totalInventoryValue.collectAsState()
    val stockDialogProduct by viewModel.stockDialogProduct.collectAsState()

    if (stockDialogProduct != null) {
        AddStockDialog(
            productName = stockDialogProduct!!.name,
            onConfirm = { qty, purchasePrice, note, date ->
                viewModel.addStock(stockDialogProduct!!.id, qty, purchasePrice, note, date)
            },
            onDismiss = { viewModel.dismissAddStockDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.ProductList.route)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Products")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditProduct.createRoute())
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        if (inventoryItems.isEmpty()) {
            EmptyStateMessage("No products in inventory. Tap + to add one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SummaryCard(
                        title = "Total Inventory Value",
                        value = CurrencyUtils.formatAmount(totalValue),
                        backgroundColor = MaterialTheme.pennyTrailColors.cardBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(inventoryItems, key = { it.product.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.AddEditProduct.createRoute(item.product.id))
                            }
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
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.showAddStockDialog(item.product) }) {
                                    Icon(
                                        Icons.Default.AddBox,
                                        contentDescription = "Add Stock",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Stock info row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Stock: ${item.totalStock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sold: ${item.totalSold}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val stockColor = when {
                                    item.availableStock <= 0 -> MaterialTheme.pennyTrailColors.stockRed
                                    item.availableStock <= 5 -> MaterialTheme.pennyTrailColors.stockAmber
                                    else -> MaterialTheme.pennyTrailColors.stockGreen
                                }
                                Text(
                                    text = "Available: ${item.availableStock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = stockColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Purchase and selling price row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Purchase Rate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (item.lastPurchasePrice > 0)
                                            CurrencyUtils.formatAmount(item.lastPurchasePrice)
                                        else "N/A",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Selling Rate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatAmount(item.sellingPrice),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
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
