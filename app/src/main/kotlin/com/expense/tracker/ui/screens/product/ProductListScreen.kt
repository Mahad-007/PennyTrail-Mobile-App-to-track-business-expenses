package com.expense.tracker.ui.screens.product

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.ui.components.AddStockDialog
import com.expense.tracker.ui.components.EmptyStateMessage
import com.expense.tracker.ui.navigation.Screen
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.factory(app.productRepository, app.saleRepository, app.stockRepository)
    )
    val productsWithStock by viewModel.productsWithStock.collectAsState()
    var stockDialogProduct by remember { mutableStateOf<ProductWithStock?>(null) }

    if (stockDialogProduct != null) {
        AddStockDialog(
            productName = stockDialogProduct!!.product.name,
            onConfirm = { qty, purchasePrice, note, date ->
                viewModel.addStock(stockDialogProduct!!.product.id, qty, purchasePrice, note, date)
                stockDialogProduct = null
            },
            onDismiss = { stockDialogProduct = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Products") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditProduct.createRoute())
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        if (productsWithStock.isEmpty()) {
            EmptyStateMessage("No products yet. Tap + to add one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productsWithStock, key = { it.product.id }) { pws ->
                    val product = pws.product
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.AddEditProduct.createRoute(product.id))
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.isActive)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (product.description.isNotBlank()) {
                                    Text(
                                        text = product.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Stock: ${pws.totalStock}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Sold: ${pws.totalSold}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val stockColor = when {
                                        pws.availableStock <= 0 -> MaterialTheme.pennyTrailColors.stockRed
                                        pws.availableStock <= 5 -> MaterialTheme.pennyTrailColors.stockAmber
                                        else -> MaterialTheme.pennyTrailColors.stockGreen
                                    }
                                    Text(
                                        text = "Available: ${pws.availableStock}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = stockColor
                                    )
                                }
                                if (!product.isActive) {
                                    Text(
                                        text = "Inactive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyUtils.formatAmount(product.defaultPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { stockDialogProduct = pws }) {
                                    Icon(
                                        Icons.Default.AddBox,
                                        contentDescription = "Add Stock",
                                        tint = MaterialTheme.colorScheme.primary
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
