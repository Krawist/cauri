package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.PendingIncome
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.service.IncomeSplitServiceImpl
import io.kotest.matchers.nulls.shouldBeNull
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

internal class IncomeSplitRepositoryImplTest {
    private val preferences = FakeUserPreferencesRepository()
    private val fixedCosts = FakeFixedCostRepository()
    private lateinit var database: CauriDatabase
    private lateinit var repository: IncomeSplitRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = IncomeSplitRepositoryImpl(
            dao = database.incomeSplitDao(),
            fixedCosts = fixedCosts,
            preferences = preferences,
            service = IncomeSplitServiceImpl(),
            clock = CLOCK,
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Pending {
        @Test
        fun `should offer the newest income of the threshold or more arrived this week`() = runTest {
            // given
            database.transactionDao().insert(income(amount = 150_000, at = "2026-11-01T09:00:00Z"))
            val newest = database.transactionDao().insert(income(amount = 20_000, at = "2026-11-02T09:00:00Z"))
            database.transactionDao().insert(income(amount = 5_000, at = "2026-11-03T09:00:00Z"))

            // when
            val pending = repository.pending.first()

            // then
            pending shouldBe PendingIncome(
                transactionId = newest,
                amount = 20_000,
                accountName = "Cash",
                counterparty = "Paul",
                occurredAt = Instant.parse("2026-11-02T09:00:00Z"),
            )
        }

        @Test
        fun `should follow the threshold the user chose`() = runTest {
            // given
            database.transactionDao().insert(income(amount = 20_000, at = "2026-11-02T09:00:00Z"))

            // when
            preferences.incomeSplit.value = IncomeSplitSettings(savingsRate = 10, threshold = 50_000)

            // then
            repository.pending.first().shouldBeNull()
        }

        @Test
        fun `should offer neither an income of last week nor the money a count found`() = runTest {
            // given
            database.transactionDao().insert(income(amount = 150_000, at = "2026-10-26T09:00:00Z"))

            // when
            database.transactionDao().insert(
                income(amount = 20_000, at = "2026-11-02T09:00:00Z").copy(origin = TransactionOrigin.RECONCILIATION),
            )

            // then
            repository.pending.first().shouldBeNull()
        }

        @Test
        fun `should offer no more an income split or not`() = runTest {
            // given
            val split = database.transactionDao().insert(income(amount = 150_000, at = "2026-11-01T09:00:00Z"))
            val dismissed = database.transactionDao().insert(income(amount = 20_000, at = "2026-11-02T09:00:00Z"))
            repository.split(split, IncomeSplit(savings = 15_000, fixedCosts = 0, budgets = 135_000))

            // when
            repository.dismiss(dismissed)

            // then
            repository.pending.first().shouldBeNull()
        }
    }

    @Nested
    inner class Proposal {
        @Test
        fun `should keep for the fixed costs not paid what earlier incomes of the month did not`() = runTest {
            // given
            fixedCosts.fixedCosts.value = listOf(
                fixedCost(id = 1, amount = 50_000, paid = false),
                fixedCost(id = 2, amount = 12_000, paid = false),
            )
            val earlier = database.transactionDao().insert(income(amount = 40_000, at = "2026-11-01T09:00:00Z"))
            repository.split(earlier, IncomeSplit(savings = 0, fixedCosts = 40_000, budgets = 0))
            val income = PendingIncome(
                transactionId = 9,
                amount = 100_000,
                accountName = "Cash",
                counterparty = null,
                occurredAt = CLOCK.instant(),
            )

            // when
            val proposal = repository.proposal(income, savingsRate = 10)

            // then
            proposal.split shouldBe IncomeSplit(savings = 10_000, fixedCosts = 22_000, budgets = 68_000)
            proposal.fixedCosts.map { it.id } shouldBe listOf(1L, 2L)
        }
    }

    private fun fixedCost(id: Long, amount: Long, paid: Boolean) = FixedCost(
        id = id,
        name = "Loyer",
        amount = amount,
        dayOfMonth = 1,
        categoryId = null,
        counterparty = null,
        paid = paid,
    )

    private fun income(amount: Long, at: String) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.INCOME,
        status = TransactionStatus.DETECTED,
        origin = TransactionOrigin.SIGNAL,
        amount = amount,
        occurredAt = Instant.parse(at),
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = null,
        counterparty = "Paul",
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
