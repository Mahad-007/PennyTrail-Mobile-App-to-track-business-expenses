package com.expense.tracker.ui.screens.sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.expense.tracker.PennyTrailApp
import com.expense.tracker.data.local.entity.PaymentType
import com.expense.tracker.ui.components.ConfirmDeleteDialog
import com.expense.tracker.ui.components.CurrencyTextField
import com.expense.tracker.ui.components.DatePickerField
import com.expense.tracker.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSaleScreen(saleId: Long, navController: NavController) {
    val app = LocalContext.current.applicationContext as PennyTrailApp
    val viewModel: SaleViewModel = viewModel(
        factory = SaleViewModel.factory(app.saleRepository, app.productRepository)
    )
    val formState by viewModel.formState.collectAsState()
    val products by viewModel.availableProducts.collectAsState()
    val isEditing = saleId != -1L
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(saleId) {
        if (isEditing) viewModel.loadSale(saleId)
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteSale(saleId) { navController.popBackStack() }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Sale" else "Add Sale") },
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
        ) {
            DatePickerField(
                label = "Date",
                dateMillis = formState.date,
                onDateSelected = { viewModel.updateDate(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (products.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.productName,
                        onValueChange = { viewModel.updateProductName(it) },
                        label = { Text("Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text("${product.name} - ${CurrencyUtils.formatAmount(product.defaultPrice)}") },
                                onClick = {
                                    viewModel.selectProduct(product)
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = formState.productName,
                    onValueChange = { viewModel.updateProductName(it) },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.quantity,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                        viewModel.updateQuantity(newValue)
                    }
                },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CurrencyTextField(
                label = "Unit Price",
                value = formState.unitPrice,
                onValueChange = { viewModel.updateUnitPrice(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total: ${CurrencyUtils.formatAmount(formState.totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = formState.paymentType == PaymentType.CASH,
                    onClick = { viewModel.updatePaymentType(PaymentType.CASH) },
                    label = { Text("Cash") }
                )
                FilterChip(
                    selected = formState.paymentType == PaymentType.CREDIT,
                    onClick = { viewModel.updatePaymentType(PaymentType.CREDIT) },
                    label = { Text("Credit (Udhar)") }
                )
            }

            AnimatedVisibility(visible = formState.paymentType == PaymentType.CREDIT) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = formState.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveSale(saleId) { navController.popBackStack() } },
                enabled = formState.productName.isNotBlank()
                        && formState.quantity.toIntOrNull() != null
                        && formState.unitPrice.toDoubleOrNull() != null
                        && (formState.paymentType == PaymentType.CASH || formState.customerName.isNotBlank()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Sale")
            }
        }
    }
}
