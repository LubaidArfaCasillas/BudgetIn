package com.iyas.budgetin.domain.repository

import com.iyas.budgetin.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category): Result<Unit>
}
