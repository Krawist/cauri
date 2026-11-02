package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryNature
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CategoryDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var categoryDao: CategoryDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        categoryDao = database.categoryDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveActive {
        @Test
        fun `should list the active categories, by name`() = runTest {
            // given
            val transportId = categoryDao.insert(
                CategoryEntity(name = "Transport", parentId = null, nature = CategoryNature.EXPENSE, status = CategoryStatus.ACTIVE),
            )
            val motoId = categoryDao.insert(
                CategoryEntity(name = "Moto", parentId = transportId, nature = CategoryNature.EXPENSE, status = CategoryStatus.ACTIVE),
            )
            categoryDao.insert(
                CategoryEntity(
                    name = "Loisirs",
                    parentId = null,
                    nature = CategoryNature.EXPENSE,
                    status = CategoryStatus.ARCHIVED,
                ),
            )

            // when
            val active = categoryDao.observeActive().first()

            // then
            active.map { it.id } shouldContainExactly listOf(motoId, transportId)
        }
    }
}
