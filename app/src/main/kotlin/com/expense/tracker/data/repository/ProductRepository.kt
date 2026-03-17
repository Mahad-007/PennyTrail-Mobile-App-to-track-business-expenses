package com.expense.tracker.data.repository

import com.expense.tracker.data.local.dao.ProductDao
import com.expense.tracker.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    fun getActiveProducts(): Flow<List<ProductEntity>> = dao.getActiveProducts()
    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAllProducts()
    suspend fun getProductById(id: Long) = dao.getProductById(id)
    suspend fun insert(product: ProductEntity) = dao.insert(product)
    suspend fun update(product: ProductEntity) = dao.update(product)
    suspend fun softDelete(id: Long) = dao.softDelete(id)
}
