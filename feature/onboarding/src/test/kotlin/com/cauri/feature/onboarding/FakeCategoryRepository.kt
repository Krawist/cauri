package com.cauri.feature.onboarding

import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.repository.CategoryRepository

/** In-memory [CategoryRepository] for tests. */
internal class FakeCategoryRepository : CategoryRepository {
    val created = mutableListOf<NewCategory>()

    override suspend fun createCategories(categories: List<NewCategory>) {
        created += categories
    }
}
