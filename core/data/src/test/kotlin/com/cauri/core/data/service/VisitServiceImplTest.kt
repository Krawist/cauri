package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.VisitEntity
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VisitServiceImplTest {
    private val service = VisitServiceImpl()

    @Nested
    inner class Covers {
        @Test
        fun `should take a message that came 15 minutes after the exit`() {
            // when
            val covers = service.covers(visit(), at = LEFT + Duration.ofMinutes(15))

            // then
            covers.shouldBeTrue()
        }

        @Test
        fun `should leave a message that came later`() {
            // when
            val covers = service.covers(visit(), at = LEFT + Duration.ofMinutes(16))

            // then
            covers.shouldBeFalse()
        }

        @Test
        fun `should leave a payment made before the entry`() {
            // when
            val covers = service.covers(visit(), at = ENTERED - Duration.ofMinutes(1))

            // then
            covers.shouldBeFalse()
        }
    }

    @Nested
    inner class UsualAmount {
        @Test
        fun `should give the median of the amounts spent there`() {
            // when
            val usual = service.usualAmount(
                listOf(2_000L, 1_500L, 3_500L, 2_500L, 3_000L).mapIndexed { index, amount ->
                    expense(amount = amount, at = ENTERED + Duration.ofDays(index.toLong()), placeId = 2)
                },
            )

            // then
            usual shouldBe 2_500L
        }

        @Test
        fun `should give the lower of the two middle amounts`() {
            // when
            val usual = service.usualAmount(
                listOf(
                    expense(amount = 3_500, at = ENTERED, placeId = 2),
                    expense(amount = 2_000, at = LEFT, placeId = 2),
                ),
            )

            // then
            usual shouldBe 2_000L
        }

        @Test
        fun `should give none before the first expense there`() {
            // when
            val usual = service.usualAmount(emptyList())

            // then
            usual.shouldBeNull()
        }
    }

    private fun visit() = VisitEntity(id = 4, placeId = 2, enteredAt = ENTERED, leftAt = LEFT)

    private fun expense(amount: Long, at: Instant, placeId: Long?) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = at,
        categoryId = null,
        placeId = placeId,
        rawSignalId = null,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val ENTERED: Instant = Instant.parse("2026-11-03T09:00:00Z")
        val LEFT: Instant = Instant.parse("2026-11-03T09:25:00Z")
    }
}
