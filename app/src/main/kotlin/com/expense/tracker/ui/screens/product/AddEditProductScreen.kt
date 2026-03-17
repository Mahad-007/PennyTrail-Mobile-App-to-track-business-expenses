package com.expense.tracker.ui.screens.product

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
import com.expense.tracker.ui.components.AddStockDialog
import com.expense.tracker.ui.components.ConfirmDeleteDialog
import com.expense.tracker.ui.components.CurrencyTextField
import com.expense.tracker.ui.theme.pennyTrailColors
import com.expense.tracker.util.CurrencyUtils
import com.expense.tracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(productId: Long, navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.factory(app.productRepository, app.saleRepository, app.stockRepository)
    )
    val formState by viewModel.formState.collectAsState()
    val productsWithStock by viewModel.productsWithStock.collectAsState()
    val stockEntries by viewModel.stockEntries.collectAsState()
    val isEditing = productId != -1L
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }

    val stockInfo = if (isEditing) {
        productsWithStock.find { it.product.id == productId }
    } else null

    LaunchedEffect(productId) {
        if (isEditing) {
            viewModel.loadProduct(productId)
            viewModel.loadStockEntries(productId)
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteProduct(productId) { navController.popBackStack() }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showStockDialog && stockInfo != null) {
        AddStockDialog(
            productName = stockInfo.product.name,
            onConfirm = { qty, purchasePrice, note, date ->
                viewModel.addStock(productId, qty, purchasePrice, note, date)
                showStockDialog = false
            },
            onDismiss = { showStockDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Product" else "Add Product") },
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
            if (isEditing && stockInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Stock Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Stock", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${stockInfo.totalStock}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sold", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${stockInfo.totalSold}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available", style = MaterialTheme.typography.labelSmall)
                                val stockColor = when {
                                    stockInfo.availableStock <= 0 -> MaterialTheme.pennyTrailColors.stockRed
                                    stockInfo.availableStock <= 5 -> MaterialTheme.pennyTrailColors.stockAmber
                                    else -> MaterialTheme.pennyTrailColors.stockGreen
                                }
                                Text(
                                    "${stockInfo.availableStock}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = stockColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showStockDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddBox, contentDescription = null)
                            Text("  Add Stock", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (stockEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Stock History",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            stockEntries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = DateUtils.formatDate(entry.date),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (entry.note.isNotBlank()) {
                                            Text(
                                                text = entry.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+${entry.quantity}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.pennyTrailColors.stockGreen
                                        )
                                        if (entry.purchasePrice > 0) {
                                            Text(
                                                text = "@ ${CurrencyUtils.formatAmount(entry.purchasePrice)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Product Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CurrencyTextField(
                label = "Default Price",
                value = formState.defaultPrice,
                onValueChange = { viewModel.updatePrice(it) }
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
                onClick = { viewModel.saveProduct(productId) { navController.popBackStack() } },
                enabled = formState.name.isNotBlank() && formState.defaultPrice.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Product")
            }
        }
    }
}
