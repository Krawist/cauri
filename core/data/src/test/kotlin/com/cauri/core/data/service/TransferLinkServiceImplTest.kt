package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TransferLinkServiceImplTest {
    private val service = TransferLinkServiceImpl()

    @Nested
    inner class Pairs {
        @Test
        fun `should pair a debit and a credit of the same amount on two accounts`() {
            // given
            val debit = debit(id = 1, amount = 100_000, at = NOON)
            val credit = credit(id = 2, amount = 100_000, at = NOON.plus(Duration.ofMinutes(1)))

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs shouldContainExactly listOf(debit to credit)
        }

        @Test
        fun `should pair a credit smaller by the operator's fee`() {
            // given
            val debit = debit(id = 1, amount = 100_000, at = NOON)
            val credit = credit(id = 2, amount = 99_000, at = NOON)

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs shouldContainExactly listOf(debit to credit)
        }

        @Test
        fun `should leave apart a credit smaller by more than 3 percent`() {
            // given
            val debit = debit(id = 1, amount = 100_000, at = NOON)
            val credit = credit(id = 2, amount = 96_000, at = NOON)

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs.shouldBeEmpty()
        }

        @Test
        fun `should leave apart a credit larger than the debit`() {
            // given
            val debit = debit(id = 1, amount = 10_000, at = NOON)
            val credit = credit(id = 2, amount = 10_100, at = NOON)

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs.shouldBeEmpty()
        }

        @Test
        fun `should leave apart two operations more than 30 minutes away`() {
            // given
            val debit = debit(id = 1, amount = 10_000, at = NOON)
            val credit = credit(id = 2, amount = 10_000, at = NOON.plus(Duration.ofMinutes(31)))

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs.shouldBeEmpty()
        }

        @Test
        fun `should leave apart a debit and a credit on the same account`() {
            // given
            val debit = debit(id = 1, amount = 10_000, at = NOON)
            val credit = credit(id = 2, amount = 10_000, at = NOON).copy(accountId = debit.accountId)

            // when
            val pairs = service.pairs(listOf(debit, credit))

            // then
            pairs.shouldBeEmpty()
        }

        @Test
        fun `should prefer the same amount, then the closest in time, and use a debit once`() {
            // given
            val withFee = debit(id = 1, amount = 10_100, at = NOON)
            val far = debit(id = 2, amount = 10_000, at = NOON.minus(Duration.ofMinutes(20)))
            val close = debit(id = 3, amount = 10_000, at = NOON.minus(Duration.ofMinutes(2)))
            val first = credit(id = 4, amount = 10_000, at = NOON)
            val second = credit(id = 5, amount = 10_000, at = NOON.plus(Duration.ofMinutes(1)))

            // when
            val pairs = service.pairs(listOf(withFee, far, close, first, second))

            // then
            pairs shouldContainExactly listOf(close to first, far to second)
        }
    }

    @Nested
    inner class Linked {
        @Test
        fun `should turn the debit and the credit into the two sides of the transfer`() {
            // given
            val debit = debit(id = 1, amount = 10_000, at = NOON)
            val credit = credit(id = 2, amount = 10_000, at = NOON)

            // when
            val transfer = service.linked(debit = debit, credit = credit, group = "g")

            // then
            transfer shouldBe LinkedTransfer(
                debit = debit.copy(type = TransactionType.TRANSFER_OUT, transferGroup = "g"),
                credit = credit.copy(type = TransactionType.TRANSFER_IN, transferGroup = "g"),
                fee = null,
            )
        }

        @Test
        fun `should take what did not arrive as a fee on the debited account`() {
            // given
            val debit = debit(id = 1, amount = 100_000, at = NOON)
            val credit = credit(id = 2, amount = 99_000, at = NOON)

            // when
            val transfer = service.linked(debit = debit, credit = credit, group = "g")

            // then
            transfer.debit.amount shouldBe 99_000
            transfer.fee shouldBe debit.copy(
                id = 0,
                amount = 1_000,
                rawSignalId = null,
                counterparty = null,
            )
        }

        @Test
        fun `should leave out the fee when both amounts are equal`() {
            // when
            val transfer = service.linked(
                debit = debit(id = 1, amount = 10_000, at = NOON),
                credit = credit(id = 2, amount = 10_000, at = NOON),
                group = "g",
            )

            // then
            transfer.fee.shouldBeNull()
        }
    }

    // A debit on UBA read in signal 10 + id.
    private fun debit(id: Long, amount: Long, at: Instant) = TransactionEntity(
        id = id,
        accountId = 3,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.DETECTED,
        origin = TransactionOrigin.SIGNAL,
        amount = amount,
        occurredAt = at,
        categoryId = null,
        placeId = null,
        rawSignalId = 10 + id,
        transferGroup = null,
        counterparty = "ORANGEMONEYCM",
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    // A credit on Orange Money.
    private fun credit(id: Long, amount: Long, at: Instant) = debit(
        id = id,
        amount = amount,
        at = at,
    ).copy(accountId = 2, type = TransactionType.INCOME, counterparty = "UBA")

    private companion object {
        val NOON: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
