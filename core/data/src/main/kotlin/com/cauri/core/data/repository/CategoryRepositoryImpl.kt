package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.model.NewCategory
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CategoryRepository]. */
@Singleton
internal class CategoryRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao) : CategoryRepository {
    override suspend fun createCategories(categories: List<NewCategory>) {
        categoryDao.insertAll(
            categories.map {
                CategoryEntity(name = it.name, parentId = null, nature = it.nature, status = CategoryStatus.ACTIVE)
            },
        )
    }
}
