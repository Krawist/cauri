package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.MonthSpending
import com.cauri.core.data.model.PendingType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ActivityServiceImplTest {
    private val service = ActivityServiceImpl()

    @Nested
    inner class Lines {
        @Test
        fun `should show a transfer once, from its account to the other`() {
            // given
            val out = transaction(id = 1, accountId = 2, type = TransactionType.TRANSFER_OUT).copy(transferGroup = "t")
            val into = transaction(id = 2, accountId = 1, type = TransactionType.TRANSFER_IN).copy(transferGroup = "t")

            // when
            val lines = service.lines(listOf(out, into), emptyList(), accounts(), emptyMap())

            // then
            lines.operations().map { listOf(it.transactionId, it.type, it.accountName, it.toAccountName) } shouldBe
                listOf(listOf(1L, PendingType.TRANSFER, "Orange Money", "Cash"))
        }

        @Test
        fun `should show a split operation once, with its whole amount`() {
            // given
            val first = transaction(id = 1, accountId = 2, type = TransactionType.EXPENSE).copy(splitGroup = "s")
            val part = transaction(id = 2, accountId = 2, type = TransactionType.EXPENSE)
                .copy(splitGroup = "s", amount = 1_000, rawSignalId = null)

            // when
            val lines = service.lines(listOf(first, part), emptyList(), accounts(), emptyMap())

            // then
            lines.operations().map { Triple(it.transactionId, it.amount, it.parts) } shouldContainExactly
                listOf(Triple(1L, 3_500L, 2))
        }

        @Test
        fun `should leave out the entries merged into a detected operation`() {
            // given
            val merged = transaction(id = 1, accountId = 1, type = TransactionType.EXPENSE)
                .copy(status = TransactionStatus.MERGED)

            // when
            val lines = service.lines(listOf(merged), emptyList(), accounts(), emptyMap())

            // then
            lines.shouldBeEmpty()
        }

        @Test
        fun `should count a fee read with its operation, never waiting for a confirmation`() {
            // given
            val fee = transaction(id = 1, accountId = 2, type = TransactionType.EXPENSE)
                .copy(status = TransactionStatus.DETECTED, rawSignalId = null)

            // when
            val lines = service.lines(listOf(fee), emptyList(), accounts(), emptyMap())

            // then
            lines.operations().map { it.fee to it.status } shouldContainExactly listOf(true to ActivityStatus.CONFIRMED)
        }

        @Test
        fun `should show a count above the expenses its gap was split into`() {
            // given
            val countedAt = Instant.ofEpochSecond(2_000)
            val count = CashCountEntity(id = 4, countedAt = countedAt, amount = 3_000, expectedAmount = 7_500)
            val gap = transaction(id = 1, accountId = 1, type = TransactionType.EXPENSE)
                .copy(origin = TransactionOrigin.RECONCILIATION, occurredAt = countedAt, rawSignalId = null)

            // when
            val lines = service.lines(listOf(gap), listOf(count), accounts(), emptyMap())

            // then
            lines.map { it::class } shouldContainExactly
                listOf(ActivityLine.CashCount::class, ActivityLine.Operation::class)
            (lines.first() as ActivityLine.CashCount).expenses shouldBe 1
        }
    }

    @Nested
    inner class Spending {
        @Test
        fun `should add up cash apart from digital, leaving out what was set aside or before`() {
            // given
            val transactions = listOf(
                transaction(id = 1, accountId = 1, type = TransactionType.EXPENSE),
                transaction(id = 2, accountId = 2, type = TransactionType.EXPENSE),
                transaction(id = 3, accountId = 2, type = TransactionType.EXPENSE)
                    .copy(status = TransactionStatus.DISMISSED),
                transaction(id = 4, accountId = 1, type = TransactionType.EXPENSE)
                    .copy(occurredAt = Instant.ofEpochSecond(10)),
                transaction(id = 5, accountId = 2, type = TransactionType.INCOME),
            )
            val lines = service.lines(transactions, emptyList(), accounts(), emptyMap())

            // when
            val spending = service.spending(lines = lines, from = Instant.ofEpochSecond(500))

            // then
            spending shouldBe MonthSpending(cash = 2_500, digital = 2_500)
        }
    }

    private fun List<ActivityLine>.operations() = filterIsInstance<ActivityLine.Operation>()

    private fun transaction(id: Long, accountId: Long, type: TransactionType) = TransactionEntity(
        id = id,
        accountId = accountId,
        type = type,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.SIGNAL,
        amount = 2_500,
        occurredAt = Instant.ofEpochSecond(1_000),
        categoryId = null,
        placeId = null,
        rawSignalId = id,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private fun accounts() = mapOf(
        1L to account(id = 1, name = "Cash", kind = AccountKind.CASH),
        2L to account(id = 2, name = "Orange Money", kind = AccountKind.MOBILE_MONEY),
    )

    private fun account(id: Long, name: String, kind: AccountKind) = AccountEntity(
        id = id,
        name = name,
        kind = kind,
        provider = name.uppercase(),
        currency = "XAF",
        status = AccountStatus.ACTIVE,
    )
}
