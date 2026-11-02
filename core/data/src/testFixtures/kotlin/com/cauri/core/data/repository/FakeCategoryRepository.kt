package com.cauri.core.data.repository

import com.cauri.core.data.model.NewCategory

/** In-memory [CategoryRepository] for tests. */
class FakeCategoryRepository : CategoryRepository {
    val created = mutableListOf<NewCategory>()

    override suspend fun createCategories(categories: List<NewCategory>) {
        created += categories
    }
}
