package com.expense.tracker.data.local.entity

data class DailySalesSummary(
    val dateMillis: Long,
    val productBreakdown: List<SalesByProduct>,
    val dailyTotal: Double
)
