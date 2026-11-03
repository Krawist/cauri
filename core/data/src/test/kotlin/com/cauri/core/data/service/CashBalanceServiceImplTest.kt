package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.CashBalance
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CashBalanceServiceImplTest {
    private val service = CashBalanceServiceImpl()

    @Nested
    inner class Estimate {
        @Test
        fun `should be the counted amount when nothing moved since`() {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 7_500, expectedAmount = null)

            // when
            val balance = service.estimate(count = count, transactions = emptyList())

            // then
            balance shouldBe
                CashBalance(amount = 7_500, countedAt = Instant.ofEpochSecond(1_000), expensesSinceCount = 0)
        }

        @ParameterizedTest
        @CsvSource("INCOME, 9500", "TRANSFER_IN, 9500", "EXPENSE, 5500", "TRANSFER_OUT, 5500")
        fun `should move the counted amount in the direction of the transaction`(
            type: TransactionType,
            expected: Long,
        ) {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 7_500, expectedAmount = null)
            val transaction = cashTransaction(type = type, amount = 2_000, at = Instant.ofEpochSecond(2_000))

            // when
            val balance = service.estimate(count = count, transactions = listOf(transaction))

            // then
            balance.amount shouldBe expected
        }

        @Test
        fun `should add a withdrawal and take away the expenses since the count`() {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 7_500, expectedAmount = null)
            val transactions = listOf(
                cashTransaction(type = TransactionType.TRANSFER_IN, amount = 20_000, at = Instant.ofEpochSecond(2_000)),
                cashTransaction(type = TransactionType.EXPENSE, amount = 500, at = Instant.ofEpochSecond(3_000)),
                cashTransaction(type = TransactionType.EXPENSE, amount = 300, at = Instant.ofEpochSecond(4_000)),
            )

            // when
            val balance = service.estimate(count = count, transactions = transactions)

            // then
            balance shouldBe
                CashBalance(amount = 26_700, countedAt = Instant.ofEpochSecond(1_000), expensesSinceCount = 2)
        }

        @Test
        fun `should ignore a dismissed expense`() {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 7_500, expectedAmount = null)
            val dismissed = cashTransaction(
                type = TransactionType.EXPENSE,
                amount = 2_000,
                at = Instant.ofEpochSecond(2_000),
                status = TransactionStatus.DISMISSED,
            )

            // when
            val balance = service.estimate(count = count, transactions = listOf(dismissed))

            // then
            balance shouldBe
                CashBalance(amount = 7_500, countedAt = Instant.ofEpochSecond(1_000), expensesSinceCount = 0)
        }

        @Test
        fun `should ignore an expense made before the count, which the count already includes`() {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 7_500, expectedAmount = null)
            val before =
                cashTransaction(type = TransactionType.EXPENSE, amount = 2_000, at = Instant.ofEpochSecond(1_000))

            // when
            val balance = service.estimate(count = count, transactions = listOf(before))

            // then
            balance.amount shouldBe 7_500
        }

        @Test
        fun `should go below zero when more was spent than counted`() {
            // given
            val count = CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 1_000, expectedAmount = null)
            val expense =
                cashTransaction(type = TransactionType.EXPENSE, amount = 1_500, at = Instant.ofEpochSecond(2_000))

            // when
            val balance = service.estimate(count = count, transactions = listOf(expense))

            // then
            balance.amount shouldBe -500
        }
    }

    // A cash transaction; only its type, status, amount and time matter to the balance.
    private fun cashTransaction(
        type: TransactionType,
        amount: Long,
        at: Instant,
        status: TransactionStatus = TransactionStatus.CONFIRMED,
    ) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = type,
        status = status,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = at,
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
