package com.expense.tracker.data.local.entity

data class SalesByProduct(
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Double,
    val sales: List<SaleEntity> = emptyList()
)
