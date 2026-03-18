package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("productId")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val productId: Long?,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val paymentType: String = PaymentType.CASH.name,
    val customerName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
