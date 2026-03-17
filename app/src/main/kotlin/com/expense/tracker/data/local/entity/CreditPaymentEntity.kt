package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_payments",
    foreignKeys = [
        ForeignKey(
            entity = CreditEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creditId")]
)
data class CreditPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditId: Long,
    val amount: Double,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
