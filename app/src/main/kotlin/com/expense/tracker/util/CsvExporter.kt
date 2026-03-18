package com.expense.tracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.expense.tracker.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter

object CsvExporter {

    suspend fun exportAllData(context: Context): Intent {
        val db = AppDatabase.getInstance(context)
        val exportDir = File(context.cacheDir, "export").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val files = mutableListOf<File>()

        // Export Expenses
        val expenses = db.expenseDao().getAllExpenses().first()
        if (expenses.isNotEmpty()) {
            val file = File(exportDir, "expenses.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Date,Amount,Category,Description")
                expenses.forEach { e ->
                    writer.appendLine("${e.id},${DateUtils.formatDate(e.date)},${e.amount},${csvEscape(e.category)},${csvEscape(e.description)}")
                }
            }
            files.add(file)
        }

        // Export Sales
        val sales = db.saleDao().getAllSales().first()
        if (sales.isNotEmpty()) {
            val file = File(exportDir, "sales.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Date,Product Name,Quantity,Unit Price,Total Amount,Payment Type,Customer Name")
                sales.forEach { s ->
                    writer.appendLine("${s.id},${DateUtils.formatDate(s.date)},${csvEscape(s.productName)},${s.quantity},${s.unitPrice},${s.totalAmount},${s.paymentType},${csvEscape(s.customerName)}")
                }
            }
            files.add(file)
        }

        // Export Products
        val products = db.productDao().getAllProducts().first()
        if (products.isNotEmpty()) {
            val file = File(exportDir, "products.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Name,Selling Price,Description,Active,Stock Quantity")
                products.forEach { p ->
                    writer.appendLine("${p.id},${csvEscape(p.name)},${p.defaultPrice},${csvEscape(p.description)},${p.isActive},${p.stockQuantity}")
                }
            }
            files.add(file)
        }

        // Export Stock Entries
        val stockEntries = db.stockEntryDao().getAllEntries().first()
        if (stockEntries.isNotEmpty()) {
            val file = File(exportDir, "stock_entries.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Product ID,Quantity,Purchase Price,Note,Date")
                stockEntries.forEach { se ->
                    writer.appendLine("${se.id},${se.productId},${se.quantity},${se.purchasePrice},${csvEscape(se.note)},${DateUtils.formatDate(se.date)}")
                }
            }
            files.add(file)
        }

        // Export Credits
        val credits = db.creditDao().getAllCredits().first()
        if (credits.isNotEmpty()) {
            val file = File(exportDir, "credits.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Person Name,Total Amount,Amount Paid,Remaining,Is Paid,Date,Description,Linked Sale ID")
                credits.forEach { c ->
                    val remaining = c.amount - c.amountPaid
                    writer.appendLine("${c.id},${csvEscape(c.personName)},${c.amount},${c.amountPaid},${remaining},${c.isPaid},${DateUtils.formatDate(c.date)},${csvEscape(c.description)},${c.linkedSaleId ?: ""}")
                }
            }
            files.add(file)
        }

        // Export Credit Payments
        val creditIds = credits.map { it.id }
        val allPayments = creditIds.flatMap { id ->
            db.creditPaymentDao().getPaymentsForCredit(id).first()
        }
        if (allPayments.isNotEmpty()) {
            val creditNameMap = credits.associate { it.id to it.personName }
            val file = File(exportDir, "credit_payments.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("ID,Credit ID,Person Name,Amount,Note,Date")
                allPayments.forEach { p ->
                    val personName = creditNameMap[p.creditId] ?: ""
                    writer.appendLine("${p.id},${p.creditId},${csvEscape(personName)},${p.amount},${csvEscape(p.note)},${DateUtils.formatDate(p.date)}")
                }
            }
            files.add(file)
        }

        // Build share intent
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/csv"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, "PennyTrail Data Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
