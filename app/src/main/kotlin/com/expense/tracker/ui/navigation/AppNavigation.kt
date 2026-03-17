package com.expense.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expense.tracker.ui.screens.credit.AddEditCreditScreen
import com.expense.tracker.ui.screens.credit.CreditListScreen
import com.expense.tracker.ui.screens.dashboard.DashboardScreen
import com.expense.tracker.ui.screens.expense.AddEditExpenseScreen
import com.expense.tracker.ui.screens.expense.ExpenseListScreen
import com.expense.tracker.ui.screens.product.AddEditProductScreen
import com.expense.tracker.ui.screens.product.ProductListScreen
import com.expense.tracker.ui.screens.inventory.InventoryScreen
import com.expense.tracker.ui.screens.sale.AddEditSaleScreen
import com.expense.tracker.ui.screens.sale.SaleListScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard.route),
    BottomNavItem("Sales", Icons.Default.PointOfSale, Screen.SaleList.route),
    BottomNavItem("Inventory", Icons.Default.Inventory, Screen.Inventory.route),
    BottomNavItem("Expenses", Icons.Default.MoneyOff, Screen.ExpenseList.route),
    BottomNavItem("Credits", Icons.Default.CreditCard, Screen.CreditList.route)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            composable(Screen.ExpenseList.route) {
                ExpenseListScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditExpense.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                AddEditExpenseScreen(expenseId = expenseId, navController = navController)
            }

            composable(Screen.SaleList.route) {
                SaleListScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditSale.route,
                arguments = listOf(navArgument("saleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: -1L
                AddEditSaleScreen(saleId = saleId, navController = navController)
            }

            composable(Screen.Inventory.route) {
                InventoryScreen(navController = navController)
            }

            composable(Screen.ProductList.route) {
                ProductListScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditProduct.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
                AddEditProductScreen(productId = productId, navController = navController)
            }

            composable(Screen.CreditList.route) {
                CreditListScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditCredit.route,
                arguments = listOf(navArgument("creditId") { type = NavType.LongType })
            ) { backStackEntry ->
                val creditId = backStackEntry.arguments?.getLong("creditId") ?: -1L
                AddEditCreditScreen(creditId = creditId, navController = navController)
            }
        }
    }
}
