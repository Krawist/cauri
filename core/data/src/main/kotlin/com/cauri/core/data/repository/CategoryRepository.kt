package com.cauri.core.data.repository

import com.cauri.core.data.model.NewCategory

/** The user's categories of transactions. */
interface CategoryRepository {
    /** Creates [categories] as active top-level categories, all or none. */
    suspend fun createCategories(categories: List<NewCategory>)
}
