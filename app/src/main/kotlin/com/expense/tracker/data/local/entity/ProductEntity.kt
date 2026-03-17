package com.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultPrice: Double,
    val description: String = "",
    val isActive: Boolean = true,
    val stockQuantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
