package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.GoalKind
import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.NewGoal
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.service.CategoryPredictionServiceImpl
import com.cauri.core.data.service.CategoryStyleServiceImpl
import com.cauri.core.data.service.CategorySuggestionServiceImpl
import com.cauri.core.data.service.FixedCostServiceImpl
import com.cauri.core.data.service.GoalServiceImpl
import com.cauri.core.data.service.IncomeSplitServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GoalRepositoryImplTest {
    private val preferences = FakeUserPreferencesRepository()
    private lateinit var database: CauriDatabase
    private lateinit var repository: GoalRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = GoalRepositoryImpl(
            dao = database.goalDao(),
            available = database.availableDao(),
            preferences = preferences,
            service = GoalServiceImpl(),
            clock = CLOCK,
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Overview {
        @Test
        fun `should give what each saving holds and send the savings of a split to the first not reached`() = runTest {
            // given
            repository.add(NewGoal(name = "Téléphone", kind = GoalKind.SAVING, amount = 10_000))
            repository.add(NewGoal(name = "Moto", kind = GoalKind.SAVING, amount = 1_200_000))
            val (phone, _) = repository.overview.first().goals.map { it.goal.id }
            repository.putAside(phone, 10_000)
            val income = database.transactionDao().insert(transaction(TransactionType.INCOME, 150_000, "11-02"))

            // when
            splits().split(income, IncomeSplit(savings = 15_000, fixedCosts = 0, budgets = 135_000))

            // then
            val overview = repository.overview.first()
            overview.goals.map { it.goal.name to it.saved } shouldContainExactly
                listOf("Téléphone" to 10_000L, "Moto" to 15_000L)
            overview.goals.map { it.receiving } shouldContainExactly listOf(false, true)
            overview.savedThisMonth shouldBe 25_000
        }

        @Test
        fun `should estimate when a saving is reached from the incomes of the past months`() = runTest {
            // given
            listOf("08-05", "09-05", "10-05").forEach {
                database.transactionDao().insert(transaction(TransactionType.INCOME, 100_000, it))
            }

            // when
            repository.add(NewGoal(name = "Moto", kind = GoalKind.SAVING, amount = 25_000))

            // then
            repository.overview.first().goals.single().estimate shouldBe YearMonth.of(2027, 2)
        }

        @Test
        fun `should give what an envelope's category spent this month`() = runTest {
            // given
            val (family) = categories().createCategories(
                listOf(NewCategory(name = "Famille", nature = CategoryNature.EXPENSE)),
            )
            database.transactionDao().insert(transaction(TransactionType.EXPENSE, 12_000, "11-03", family))
            database.transactionDao().insert(transaction(TransactionType.EXPENSE, 5_000, "10-30", family))

            // when
            repository.add(
                NewGoal(name = "Imprévus famille", kind = GoalKind.ENVELOPE, amount = 30_000, categoryId = family),
            )

            // then
            repository.overview.first().goals.single().saved shouldBe 12_000
        }
    }

    @Nested
    inner class Add {
        @Test
        fun `should make the contribution of a tontine a fixed cost`() = runTest {
            // when
            repository.add(
                NewGoal(
                    name = "Tontine du quartier",
                    kind = GoalKind.TONTINE,
                    amount = 25_000,
                    payoutDate = LocalDate.parse("2026-12-15"),
                    dayOfMonth = 10,
                ),
            )

            // then
            repository.overview.first().goals.single().goal.dayOfMonth shouldBe 10
            fixedCosts().fixedCosts.first().map { it.name to it.amount } shouldContainExactly
                listOf("Tontine du quartier" to 25_000L)
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete a tontine and the fixed cost of its contribution`() = runTest {
            // given
            repository.add(NewGoal(name = "Tontine", kind = GoalKind.TONTINE, amount = 25_000, dayOfMonth = 10))
            val id = repository.overview.first().goals.single().goal.id

            // when
            repository.delete(id)

            // then
            repository.overview.first().goals.shouldBeEmpty()
            fixedCosts().fixedCosts.first().shouldBeEmpty()
        }
    }

    @Nested
    inner class MoveFirst {
        @Test
        fun `should put a goal first`() = runTest {
            // given
            repository.add(NewGoal(name = "Téléphone", kind = GoalKind.SAVING, amount = 10_000))
            repository.add(NewGoal(name = "Moto", kind = GoalKind.SAVING, amount = 1_200_000))
            val moto = repository.overview.first().goals.last().goal.id

            // when
            repository.moveFirst(moto)

            // then
            repository.overview.first().goals.map { it.goal.name } shouldContainExactly listOf("Moto", "Téléphone")
        }
    }

    @Nested
    inner class CreateEmergencyFund {
        @Test
        fun `should create it first, a month of spending rounded up to 5 000 F, once`() = runTest {
            // given
            repository.add(NewGoal(name = "Moto", kind = GoalKind.SAVING, amount = 1_200_000))
            database.transactionDao().insert(transaction(TransactionType.EXPENSE, 81_000, "11-01"))
            repository.createEmergencyFund("Fonds d'urgence")

            // when
            repository.createEmergencyFund("Fonds d'urgence")

            // then
            repository.overview.first().goals.map { it.goal.name to it.goal.amount } shouldContainExactly
                listOf("Fonds d'urgence" to 85_000L, "Moto" to 1_200_000L)
        }
    }

    private fun splits() = IncomeSplitRepositoryImpl(
        dao = database.incomeSplitDao(),
        goals = database.goalDao(),
        fixedCosts = fixedCosts(),
        preferences = preferences,
        service = IncomeSplitServiceImpl(),
        clock = CLOCK,
    )

    private fun fixedCosts() = FixedCostRepositoryImpl(database.fixedCostDao(), FixedCostServiceImpl(), CLOCK)

    private fun categories() = CategoryRepositoryImpl(
        database.categoryDao(),
        CategorySuggestionServiceImpl(),
        CategoryPredictionServiceImpl(),
        CategoryStyleServiceImpl(),
        CLOCK,
    )

    private fun transaction(type: TransactionType, amount: Long, day: String, categoryId: Long? = null) =
        TransactionEntity(
            accountId = CASH_ACCOUNT_ID,
            type = type,
            status = TransactionStatus.CONFIRMED,
            origin = TransactionOrigin.MANUAL,
            amount = amount,
            occurredAt = Instant.parse("2026-${day}T10:00:00Z"),
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
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC)
    }
}
