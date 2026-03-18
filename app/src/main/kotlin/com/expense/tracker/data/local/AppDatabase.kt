package com.expense.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expense.tracker.data.local.dao.CreditDao
import com.expense.tracker.data.local.dao.CreditPaymentDao
import com.expense.tracker.data.local.dao.ExpenseDao
import com.expense.tracker.data.local.dao.ProductDao
import com.expense.tracker.data.local.dao.SaleDao
import com.expense.tracker.data.local.dao.StockEntryDao
import com.expense.tracker.data.local.entity.CreditEntity
import com.expense.tracker.data.local.entity.CreditPaymentEntity
import com.expense.tracker.data.local.entity.ExpenseEntity
import com.expense.tracker.data.local.entity.ProductEntity
import com.expense.tracker.data.local.entity.SaleEntity
import com.expense.tracker.data.local.entity.StockEntryEntity

@Database(
    entities = [
        ExpenseEntity::class,
        SaleEntity::class,
        ProductEntity::class,
        CreditEntity::class,
        StockEntryEntity::class,
        CreditPaymentEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun saleDao(): SaleDao
    abstract fun productDao(): ProductDao
    abstract fun creditDao(): CreditDao
    abstract fun stockEntryDao(): StockEntryDao
    abstract fun creditPaymentDao(): CreditPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS stock_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "productId INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "note TEXT NOT NULL DEFAULT '', " +
                    "date INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_entries_productId ON stock_entries(productId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Inventory: add purchase price to stock entries
                db.execSQL("ALTER TABLE stock_entries ADD COLUMN purchasePrice REAL NOT NULL DEFAULT 0.0")

                // Credits: add amountPaid for partial payment tracking
                db.execSQL("ALTER TABLE credits ADD COLUMN amountPaid REAL NOT NULL DEFAULT 0.0")

                // Backfill: already-paid credits should have amountPaid = amount
                db.execSQL("UPDATE credits SET amountPaid = amount WHERE isPaid = 1")

                // Credits: create payment history table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS credit_payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "creditId INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "note TEXT NOT NULL DEFAULT '', " +
                    "date INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "FOREIGN KEY(creditId) REFERENCES credits(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_payments_creditId ON credit_payments(creditId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Sales: add payment type (CASH/CREDIT) and customer name for credit sales
                db.execSQL("ALTER TABLE sales ADD COLUMN paymentType TEXT NOT NULL DEFAULT 'CASH'")
                db.execSQL("ALTER TABLE sales ADD COLUMN customerName TEXT NOT NULL DEFAULT ''")

                // Credits: add linked sale ID for auto-created credits
                db.execSQL("ALTER TABLE credits ADD COLUMN linkedSaleId INTEGER DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "penny_trail.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
