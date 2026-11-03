package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.service.MonthExpense
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class InsightsDaoTest {
    private lateinit var database: CauriDatabase

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetExpenses {
        @Test
        fun `should give the expenses counted in the month, cash and the rest of the counts told`() = runTest {
            // given
            val transactions = database.transactionDao()
            transactions.insert(cashExpense(amount = 2_000, origin = TransactionOrigin.MANUAL))
            transactions.insert(cashExpense(amount = 4_100, origin = TransactionOrigin.RECONCILIATION))
            transactions.insert(
                cashExpense(amount = 900, origin = TransactionOrigin.MANUAL).copy(status = TransactionStatus.DISMISSED),
            )
            transactions.insert(
                cashExpense(amount = 0, origin = TransactionOrigin.VISIT).copy(status = TransactionStatus.DETECTED),
            )

            // when
            val expenses = database.insightsDao().getExpenses(
                from = Instant.parse("2026-11-01T00:00:00Z"),
                to = Instant.parse("2026-12-01T00:00:00Z"),
            )

            // then
            expenses shouldContainExactlyInAnyOrder listOf(
                MonthExpense(categoryId = null, amount = 2_000, cash = true, unidentified = false),
                MonthExpense(categoryId = null, amount = 4_100, cash = true, unidentified = true),
            )
        }
    }

    private fun cashExpense(amount: Long, origin: TransactionOrigin) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = origin,
        amount = amount,
        occurredAt = Instant.parse("2026-11-03T12:00:00Z"),
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )
}
