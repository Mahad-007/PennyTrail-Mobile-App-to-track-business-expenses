package com.expense.tracker.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return "Rs. ${formatter.format(amount)}"
    }

    fun formatAmountShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "Rs. ${"%.1f".format(amount / 1_000_000)}M"
            amount >= 1_000 -> "Rs. ${"%.1f".format(amount / 1_000)}K"
            else -> formatAmount(amount)
        }
    }
}
