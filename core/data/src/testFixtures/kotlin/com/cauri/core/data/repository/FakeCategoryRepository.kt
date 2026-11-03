package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.NewCategory
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CategoryRepository] for tests: created categories get the ids 1, 2, 3… in order. */
class FakeCategoryRepository : CategoryRepository {
    val created = mutableListOf<NewCategory>()

    override val categories = MutableStateFlow<List<Category>>(emptyList())

    override suspend fun createCategories(categories: List<NewCategory>): List<Long> {
        val first = created.size + 1L
        created += categories
        return categories.indices.map { first + it }
    }
}
