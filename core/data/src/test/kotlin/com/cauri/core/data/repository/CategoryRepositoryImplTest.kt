package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewCategory
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CategoryRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: CategoryRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = CategoryRepositoryImpl(database.categoryDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class CreateCategories {
        @Test
        fun `should create active top-level categories with their nature`() = runTest {
            // when
            repository.createCategories(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Tontine", nature = CategoryNature.SAVING),
                ),
            )

            // then
            database.categoryDao().observeActive().first().map {
                Triple(it.name, it.nature, it.parentId)
            } shouldContainExactlyInAnyOrder
                listOf(
                    Triple("Transport", CategoryNature.EXPENSE, null),
                    Triple("Tontine", CategoryNature.SAVING, null),
                )
        }

        @Test
        fun `should create them as active`() = runTest {
            // when
            repository.createCategories(listOf(NewCategory(name = "Santé", nature = CategoryNature.EXPENSE)))

            // then
            database.categoryDao().observeActive().first().map { it.status } shouldContainExactlyInAnyOrder
                listOf(CategoryStatus.ACTIVE)
        }
    }
}
