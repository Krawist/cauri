package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class DuplicateServiceImplTest {
    private val service = DuplicateServiceImpl()

    @Nested
    inner class SameOperation {
        @Test
        fun `should find the detected transaction of a manual entry`() {
            // given
            val detected = detected(id = 1, at = NOON.plus(Duration.ofMinutes(40)))

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(detected))

            // then
            found shouldBe detected
        }

        @Test
        fun `should find the manual entry of a detected transaction`() {
            // given
            val manual = manual(id = 2, at = NOON)

            // when
            val found = service.sameOperation(transaction = detected(id = 1, at = NOON), candidates = listOf(manual))

            // then
            found shouldBe manual
        }

        @Test
        fun `should take the closest in time when several match`() {
            // given
            val far = detected(id = 1, at = NOON.plus(Duration.ofMinutes(90)))
            val close = detected(id = 3, at = NOON.minus(Duration.ofMinutes(10)))

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(far, close))

            // then
            found shouldBe close
        }

        @Test
        fun `should leave apart two operations more than two hours away`() {
            // given
            val detected = detected(id = 1, at = NOON.plus(Duration.ofMinutes(121)))

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(detected))

            // then
            found.shouldBeNull()
        }

        @Test
        fun `should leave apart another amount`() {
            // given
            val detected = detected(id = 1, at = NOON).copy(amount = 2_600)

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(detected))

            // then
            found.shouldBeNull()
        }

        @Test
        fun `should leave apart two manual entries`() {
            // given
            val other = manual(id = 1, at = NOON)

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(other))

            // then
            found.shouldBeNull()
        }

        @Test
        fun `should leave apart a fee, which has no signal of its own`() {
            // given
            val fee = detected(id = 1, at = NOON).copy(rawSignalId = null)

            // when
            val found = service.sameOperation(transaction = manual(id = 2, at = NOON), candidates = listOf(fee))

            // then
            found.shouldBeNull()
        }

        @ParameterizedTest
        @EnumSource(TransactionType::class, names = ["TRANSFER_IN", "TRANSFER_OUT"])
        fun `should leave transfers to their own linking`(type: TransactionType) {
            // given
            val detected = detected(id = 1, at = NOON).copy(type = type)

            // when
            val found = service.sameOperation(
                transaction = manual(id = 2, at = NOON).copy(type = type),
                candidates = listOf(detected),
            )

            // then
            found.shouldBeNull()
        }
    }

    @Nested
    inner class Merged {
        @Test
        fun `should keep the message's data with the user's category and note, confirmed`() {
            // given
            val detected = detected(id = 1, at = NOON).copy(categoryId = null, note = null)
            val manual = manual(id = 2, at = NOON).copy(categoryId = 4, note = "Pain")

            // when
            val merged = service.merged(detected = detected, manual = manual)

            // then
            merged shouldBe detected.copy(status = TransactionStatus.CONFIRMED, categoryId = 4, note = "Pain")
        }
    }

    // An Orange Money payment of 2 500 read in signal 9.
    private fun detected(id: Long, at: Instant) = TransactionEntity(
        id = id,
        accountId = 2,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.DETECTED,
        origin = TransactionOrigin.SIGNAL,
        amount = 2_500,
        occurredAt = at,
        categoryId = null,
        placeId = null,
        rawSignalId = 9,
        transferGroup = null,
        counterparty = "BOULANGERIE",
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    // The same payment typed by the user.
    private fun manual(id: Long, at: Instant) = detected(id = id, at = at).copy(
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        rawSignalId = null,
        counterparty = null,
    )

    private companion object {
        val NOON: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
