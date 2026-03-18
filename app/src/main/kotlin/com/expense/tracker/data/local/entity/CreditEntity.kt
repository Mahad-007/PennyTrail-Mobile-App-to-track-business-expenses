package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credits")
data class CreditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val description: String = "",
    val date: Long,
    val amountPaid: Double = 0.0,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val linkedSaleId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
