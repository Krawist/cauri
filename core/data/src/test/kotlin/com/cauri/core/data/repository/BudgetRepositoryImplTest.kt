package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.service.BudgetServiceImpl
import com.cauri.core.data.service.CategoryPredictionServiceImpl
import com.cauri.core.data.service.CategoryStyleServiceImpl
import com.cauri.core.data.service.CategorySuggestionServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BudgetRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var categories: CategoryRepositoryImpl
    private lateinit var repository: BudgetRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        categories = CategoryRepositoryImpl(
            database.categoryDao(),
            CategorySuggestionServiceImpl(),
            CategoryPredictionServiceImpl(),
            CategoryStyleServiceImpl(),
            CLOCK,
        )
        repository = BudgetRepositoryImpl(database.budgetDao(), categories, BudgetServiceImpl(), CLOCK)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Suggestions {
        @Test
        fun `should suggest a budget for each expense category spent on, after four weeks`() = runTest {
            // given
            val (foodId, _) = categories.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                ),
            )
            listOf(40L, 20L, 5L).forEach { database.transactionDao().insert(expense(foodId, daysAgo = it)) }

            // when
            val suggestions = repository.suggestions()

            // then
            suggestions shouldContainExactly listOf(
                BudgetSuggestion(categoryId = foodId, categoryName = "Alimentation", amount = 2_500),
            )
        }

        @Test
        fun `should suggest nothing again for a category budgeted or ignored`() = runTest {
            // given
            val (foodId, transportId) = categories.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                ),
            )
            listOf(40L, 20L, 5L).forEach {
                database.transactionDao().insert(expense(foodId, daysAgo = it))
                database.transactionDao().insert(expense(transportId, daysAgo = it))
            }
            repository.setBudget(categoryId = foodId, amount = 5_000, suggested = 3_000)

            // when
            repository.ignore(transportId)

            // then
            repository.suggestions().shouldBeEmpty()
        }
    }

    @Nested
    inner class Budgets {
        @Test
        fun `should give the budgets that hold, the ignored ones left out`() = runTest {
            // given
            val (foodId, transportId) = categories.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                ),
            )
            repository.setBudget(categoryId = foodId, amount = 55_000, suggested = 52_000)

            // when
            repository.ignore(transportId)

            // then
            repository.budgets.first() shouldContainExactly
                listOf(Budget(categoryId = foodId, categoryName = "Alimentation", amount = 55_000))
        }
    }

    private fun expense(categoryId: Long, daysAgo: Long) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = 1_000,
        occurredAt = CLOCK.instant() - Duration.ofDays(daysAgo),
        categoryId = categoryId,
        placeId = null,
        rawSignalId = null,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
