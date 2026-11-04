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
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.service.AvailableServiceImpl
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

internal class AvailableRepositoryImplTest {
    private val fixedCosts = FakeFixedCostRepository()
    private lateinit var database: CauriDatabase
    private lateinit var repository: AvailableRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = AvailableRepositoryImpl(
            dao = database.availableDao(),
            splits = database.incomeSplitDao(),
            fixedCosts = fixedCosts,
            service = AvailableServiceImpl(),
            clock = CLOCK,
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Today {
        @Test
        fun `should take the savings, the fixed costs not paid and the spending from the money of the month`() =
            runTest {
                // given
                val income = database.transactionDao().insert(transaction(TransactionType.INCOME, 160_000, "11-02"))
                database.transactionDao().insert(transaction(TransactionType.INCOME, 90_000, "10-28"))
                database.transactionDao().insert(transaction(TransactionType.EXPENSE, 10_000, "11-03"))
                database.transactionDao().insert(transaction(TransactionType.TRANSFER_IN, 40_000, "11-03"))
                IncomeSplitRepositoryImpl(
                    dao = database.incomeSplitDao(),
                    fixedCosts = fixedCosts,
                    preferences = FakeUserPreferencesRepository(),
                    service = IncomeSplitServiceImpl(),
                    clock = CLOCK,
                ).split(income, IncomeSplit(savings = 16_000, fixedCosts = 50_000, budgets = 94_000))

                // when
                fixedCosts.fixedCosts.value = listOf(fixedCost(amount = 50_000, paid = false), fixedCost(12_000, true))

                // then
                repository.today.first() shouldBe 3_100
            }

        @Test
        fun `should be unknown before the first income of the month`() = runTest {
            // when
            database.transactionDao().insert(transaction(TransactionType.EXPENSE, 10_000, "11-03"))

            // then
            repository.today.first().shouldBeNull()
        }
    }

    private fun fixedCost(amount: Long, paid: Boolean) = FixedCost(
        id = 1,
        name = "Loyer",
        amount = amount,
        dayOfMonth = 1,
        categoryId = null,
        counterparty = null,
        paid = paid,
    )

    private fun transaction(type: TransactionType, amount: Long, day: String) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = type,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = Instant.parse("2026-${day}T10:00:00Z"),
        categoryId = null,
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
