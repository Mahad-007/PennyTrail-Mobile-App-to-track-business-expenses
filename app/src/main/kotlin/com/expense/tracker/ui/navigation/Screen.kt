package com.expense.tracker.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object ExpenseList : Screen("expenses")
    data object AddEditExpense : Screen("expense/{expenseId}") {
        fun createRoute(expenseId: Long = -1L) = "expense/$expenseId"
    }
    data object SaleList : Screen("sales")
    data object AddEditSale : Screen("sale/{saleId}") {
        fun createRoute(saleId: Long = -1L) = "sale/$saleId"
    }
    data object ProductList : Screen("products")
    data object AddEditProduct : Screen("product/{productId}") {
        fun createRoute(productId: Long = -1L) = "product/$productId"
    }
    data object Inventory : Screen("inventory")
    data object CreditList : Screen("credits")
    data object AddEditCredit : Screen("credit/{creditId}") {
        fun createRoute(creditId: Long = -1L) = "credit/$creditId"
    }
}
