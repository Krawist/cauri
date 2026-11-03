package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.NewCategory
import kotlinx.coroutines.flow.Flow

/** The user's categories of transactions. */
interface CategoryRepository {
    /** The active categories, by name, again after each change. */
    val categories: Flow<List<Category>>

    /**
     * Creates [categories] as active top-level categories, all or none, and returns their ids in the same order.
     */
    suspend fun createCategories(categories: List<NewCategory>): List<Long>
}
