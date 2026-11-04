package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.service.CategoryPredictionServiceImpl
import com.cauri.core.data.service.CategoryStyleServiceImpl
import com.cauri.core.data.service.CategorySuggestionServiceImpl
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
        repository = CategoryRepositoryImpl(
            database.categoryDao(),
            CategorySuggestionServiceImpl(),
            CategoryPredictionServiceImpl(),
            CategoryStyleServiceImpl(),
            CLOCK,
        )
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

        @Test
        fun `should return the ids of the categories in their order`() = runTest {
            // when
            val ids = repository.createCategories(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                ),
            )

            // then
            repository.categories.first().associate { it.name to it.id } shouldBe
                mapOf("Transport" to ids[0], "Alimentation" to ids[1])
        }
    }

    @Nested
    inner class Categories {
        @Test
        fun `should list the active categories in the order they were created`() = runTest {
            // given
            repository.createCategories(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                ),
            )

            // when
            val categories = repository.categories.first()

            // then
            categories.map { it.name } shouldContainExactly listOf("Transport", "Alimentation")
        }

        @Test
        fun `should give a category the user did not dress Cauri's icon and colour`() = runTest {
            // when
            repository.createCategories(listOf(NewCategory(name = "Transport", nature = CategoryNature.EXPENSE)))

            // then
            repository.categories.first().single().let { it.icon to it.color } shouldBe
                (CategoryIcon.TRANSPORT to CategoryColor.GOLD)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should rename and dress the category`() = runTest {
            // given
            val (id) = repository.createCategories(
                listOf(NewCategory(name = "Transport", nature = CategoryNature.EXPENSE)),
            )

            // when
            repository.update(id, name = " Déplacements ", icon = CategoryIcon.HOUSE, color = CategoryColor.CORAL)

            // then
            repository.categories.first().single().let { Triple(it.name, it.icon, it.color) } shouldBe
                Triple("Déplacements", CategoryIcon.HOUSE, CategoryColor.CORAL)
        }
    }

    @Nested
    inner class Archive {
        @Test
        fun `should archive a category with its sub-categories, and bring them back`() = runTest {
            // given
            val (transport) = repository.createCategories(
                listOf(NewCategory(name = "Transport", nature = CategoryNature.EXPENSE)),
            )
            repository.createCategories(
                listOf(NewCategory(name = "Taxi", nature = CategoryNature.EXPENSE, parentId = transport)),
            )
            repository.archive(transport)

            // when
            val archived = repository.archived.first().map { it.name }
            repository.restore(transport)

            // then
            archived shouldContainExactly listOf("Taxi", "Transport")
            repository.categories.first().map { it.name } shouldContainExactly listOf("Transport", "Taxi")
        }
    }

    @Nested
    inner class Reorder {
        @Test
        fun `should keep the order the user chose`() = runTest {
            // given
            val ids = repository.createCategories(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                ),
            )

            // when
            repository.reorder(ids.reversed())

            // then
            repository.categories.first().map { it.name } shouldContainExactly listOf("Alimentation", "Transport")
        }
    }

    @Nested
    inner class Merge {
        @Test
        fun `should move the transactions and the sub-categories into the other, then delete it`() = runTest {
            // given
            val (food, snacks) = repository.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Snacks", nature = CategoryNature.EXPENSE),
                ),
            )
            repository.createCategories(
                listOf(NewCategory(name = "Beignets", nature = CategoryNature.EXPENSE, parentId = snacks)),
            )
            val expense = database.transactionDao().insert(
                cashExpense(categoryId = snacks, at = "2026-11-02T08:00:00Z"),
            )

            // when
            repository.merge(from = snacks, into = food)

            // then
            database.transactionDao().getById(expense)?.categoryId shouldBe food
            repository.categories.first().map { it.name to it.parentId } shouldContainExactly
                listOf("Alimentation" to null, "Beignets" to food)
        }
    }

    @Nested
    inner class MonthTotals {
        @Test
        fun `should count what each category spent this month`() = runTest {
            // given
            val (food) = repository.createCategories(
                listOf(NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE)),
            )
            database.transactionDao().insert(cashExpense(categoryId = food, at = "2026-11-01T08:00:00Z"))

            // when
            database.transactionDao().insert(cashExpense(categoryId = food, at = "2026-10-31T08:00:00Z"))

            // then
            repository.monthTotals.first() shouldBe mapOf(food to 500L)
        }
    }

    @Nested
    inner class Suggested {
        @Test
        fun `should put first the category used on the account around this time`() = runTest {
            // given
            val (_, transportId) = repository.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Santé", nature = CategoryNature.EXPENSE),
                ),
            )
            database.transactionDao().insert(cashExpense(categoryId = transportId, at = "2026-11-02T08:00:00Z"))

            // when
            val suggested = repository.suggested(accountId = CASH_ACCOUNT_ID, type = EntryType.EXPENSE, limit = 2)

            // then
            suggested.map { it.name } shouldContainExactly listOf("Transport", "Alimentation")
        }

        @Test
        fun `should suggest only income categories for an income`() = runTest {
            // given
            repository.createCategories(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Salaire", nature = CategoryNature.INCOME),
                ),
            )

            // when
            val suggested = repository.suggested(accountId = CASH_ACCOUNT_ID, type = EntryType.INCOME, limit = 3)

            // then
            suggested.map { it.name } shouldContainExactly listOf("Salaire")
        }
    }

    @Nested
    inner class Predicted {
        @Test
        fun `should put first what the user chose before with the merchant, case ignored`() = runTest {
            // given
            val (foodId, snacksId) = repository.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Snacks", nature = CategoryNature.EXPENSE),
                ),
            )
            database.transactionDao().insert(
                cashExpense(
                    categoryId = snacksId,
                    at = "2026-10-20T12:00:00Z",
                ).copy(counterparty = "BOULANGERIE SAKER"),
            )
            database.transactionDao().insert(cashExpense(categoryId = foodId, at = "2026-11-02T08:00:00Z"))

            // when
            val predicted = repository.predicted(
                operation = expenseToConfirm(counterparty = "Boulangerie Saker ", placeId = null, categoryId = null),
                limit = 2,
            )

            // then
            predicted.map { it.name } shouldContainExactly listOf("Snacks", "Alimentation")
        }

        @Test
        fun `should put what was chosen at the place before its own category`() = runTest {
            // given
            val (foodId, snacksId) = repository.createCategories(
                listOf(
                    NewCategory(name = "Alimentation", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Snacks", nature = CategoryNature.EXPENSE),
                ),
            )
            val placeId = database.placeDao().insert(
                PlaceEntity(
                    name = "Marché Mokolo",
                    latitude = 3.88,
                    longitude = 11.5,
                    radiusMeters = 80f,
                    kind = PlaceKind.SHOP,
                    defaultCategoryId = foodId,
                ),
            )
            database.transactionDao().insert(
                cashExpense(categoryId = snacksId, at = "2026-10-30T12:00:00Z").copy(placeId = placeId),
            )

            // when
            val predicted = repository.predicted(
                operation = expenseToConfirm(counterparty = null, placeId = placeId, categoryId = foodId),
                limit = 2,
            )

            // then
            predicted.map { it.name } shouldContainExactly listOf("Snacks", "Alimentation")
        }
    }

    private fun expenseToConfirm(counterparty: String?, placeId: Long?, categoryId: Long?) = PendingOperation(
        transactionId = 99,
        accountId = CASH_ACCOUNT_ID,
        type = PendingType.EXPENSE,
        amount = 0,
        fees = 0,
        foreignAmount = null,
        foreignCurrency = null,
        accountName = "Cash",
        accountKind = AccountKind.CASH,
        toAccountName = null,
        toAccountKind = null,
        counterparty = counterparty,
        occurredAt = CLOCK.instant(),
        categoryId = categoryId,
        placeId = placeId,
    )

    private fun cashExpense(categoryId: Long, at: String) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = 500,
        occurredAt = Instant.parse(at),
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
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T08:30:00Z"), ZoneOffset.UTC)
    }
}
