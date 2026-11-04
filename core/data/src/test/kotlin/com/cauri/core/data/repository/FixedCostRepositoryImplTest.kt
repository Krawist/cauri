package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.NewFixedCost
import com.cauri.core.data.service.FixedCostServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
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

internal class FixedCostRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: FixedCostRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = FixedCostRepositoryImpl(database.fixedCostDao(), FixedCostServiceImpl(), CLOCK)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class FixedCosts {
        @Test
        fun `should tell the fixed costs paid this month`() = runTest {
            // given
            repository.add(NewFixedCost(name = "Électricité", amount = 12_000, dayOfMonth = 5, counterparty = "ENEO"))
            repository.add(NewFixedCost(name = "Loyer", amount = 50_000, dayOfMonth = 1))
            database.transactionDao().insert(
                expense(counterparty = "ENEO", amount = 11_500, at = "2026-11-02T10:00:00Z"),
            )
            database.transactionDao().insert(expense(counterparty = null, amount = 50_000, at = "2026-10-31T10:00:00Z"))

            // when
            val costs = repository.fixedCosts.first()

            // then
            costs.map { it.name to it.paid } shouldContainExactly listOf("Loyer" to false, "Électricité" to true)
        }

        @Test
        fun `should replace a fixed cost changed by the user`() = runTest {
            // given
            repository.add(NewFixedCost(name = "Loyer", amount = 50_000, dayOfMonth = 1))
            val id = repository.fixedCosts.first().single().id

            // when
            repository.update(id, NewFixedCost(name = "Loyer", amount = 55_000, dayOfMonth = 3))

            // then
            repository.fixedCosts.first() shouldContainExactly listOf(
                FixedCost(
                    id = id,
                    name = "Loyer",
                    amount = 55_000,
                    dayOfMonth = 3,
                    categoryId = null,
                    counterparty = null,
                    paid = false,
                ),
            )
        }

        @Test
        fun `should forget a fixed cost deleted`() = runTest {
            // given
            repository.add(NewFixedCost(name = "Loyer", amount = 50_000, dayOfMonth = 1))
            val id = repository.fixedCosts.first().single().id

            // when
            repository.delete(id)

            // then
            repository.fixedCosts.first().shouldBeEmpty()
        }
    }

    @Nested
    inner class Suggestions {
        @Test
        fun `should suggest a merchant paid every month`() = runTest {
            // given
            listOf("2026-09-06T10:00:00Z", "2026-10-06T10:00:00Z").forEach {
                database.transactionDao().insert(expense(counterparty = "ENEO", amount = 12_000, at = it))
            }

            // when
            val suggestions = repository.suggestions.first()

            // then
            suggestions.map { it.cost.name to it.months } shouldContainExactly listOf("ENEO" to 2)
        }

        @Test
        fun `should suggest no more a merchant confirmed or refused`() = runTest {
            // given
            listOf("2026-09-06T10:00:00Z", "2026-10-06T10:00:00Z").forEach {
                database.transactionDao().insert(expense(counterparty = "ENEO", amount = 12_000, at = it))
                database.transactionDao().insert(expense(counterparty = "Canal+", amount = 8_000, at = it))
            }
            val (eneo, canal) = repository.suggestions.first().map { it.cost }
            repository.add(eneo)

            // when
            repository.ignore(canal)

            // then
            repository.suggestions.first().shouldBeEmpty()
            repository.fixedCosts.first().map { it.name } shouldBe listOf("ENEO")
        }
    }

    private fun expense(counterparty: String?, amount: Long, at: String) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = Instant.parse(at),
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = null,
        counterparty = counterparty,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
