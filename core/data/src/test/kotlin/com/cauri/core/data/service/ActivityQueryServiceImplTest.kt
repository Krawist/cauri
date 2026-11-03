package com.cauri.core.data.service

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.Spending
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ActivityQueryServiceImplTest {
    private val service = ActivityQueryServiceImpl()

    @Nested
    inner class Matches {
        @ParameterizedTest
        @ValueSource(strings = ["boulangerie", "BOULANGERIE", "  boul ", "Saker"])
        fun `should find the merchant, case and spaces ignored`(query: String) {
            // when
            val found = service.matches(line = payment(), query = query)

            // then
            found.shouldBeTrue()
        }

        @Test
        fun `should find the category, accents ignored`() {
            // when
            val found = service.matches(line = payment(), query = "deplacement")

            // then
            found.shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(strings = ["5000", "5 000", "5 000"])
        fun `should find an amount written with or without spaces`(query: String) {
            // when
            val found = service.matches(line = payment(), query = query)

            // then
            found.shouldBeTrue()
        }

        @Test
        fun `should leave out a line that does not answer`() {
            // when
            val found = service.matches(line = payment(), query = "pharmacie")

            // then
            found.shouldBeFalse()
        }

        @Test
        fun `should match every line with a blank query`() {
            // when
            val found = service.matches(line = payment(), query = " ")

            // then
            found.shouldBeTrue()
        }
    }

    @Nested
    inner class Days {
        @Test
        fun `should start this week on Monday`() {
            // when
            val days = service.days(period = ActivityPeriod.ThisWeek, today = LocalDate.of(2026, 11, 5))

            // then
            days shouldBe LocalDate.of(2026, 11, 2)..LocalDate.of(2026, 11, 5)
        }

        @Test
        fun `should cover the whole of last month`() {
            // when
            val days = service.days(period = ActivityPeriod.LastMonth, today = LocalDate.of(2026, 3, 15))

            // then
            days shouldBe LocalDate.of(2026, 2, 1)..LocalDate.of(2026, 2, 28)
        }

        @Test
        fun `should put two chosen days in order`() {
            // when
            val days = service.days(
                period = ActivityPeriod.Between(from = LocalDate.of(2026, 11, 3), to = LocalDate.of(2026, 10, 20)),
                today = LocalDate.of(2026, 11, 5),
            )

            // then
            days shouldBe LocalDate.of(2026, 10, 20)..LocalDate.of(2026, 11, 3)
        }

        @Test
        fun `should not bound the whole history`() {
            // when
            val days = service.days(period = ActivityPeriod.All, today = LocalDate.of(2026, 11, 5))

            // then
            days.shouldBeNull()
        }
    }

    @Nested
    inner class SpendingOf {
        @Test
        fun `should add up cash apart from digital, leaving out what was set aside and the income`() {
            // given
            val lines = listOf(
                payment(),
                payment().copy(accountKind = AccountKind.MOBILE_MONEY),
                payment().copy(status = ActivityStatus.SET_ASIDE),
                payment().copy(type = PendingType.INCOME),
            )

            // when
            val spending = service.spending(lines)

            // then
            spending shouldBe Spending(cash = 5_000, digital = 5_000)
        }

        @Test
        fun `should count the fees of a transfer, never the transfer`() {
            // given
            val withdrawal = payment().copy(
                type = PendingType.TRANSFER,
                accountKind = AccountKind.MOBILE_MONEY,
                fees = 150,
            )

            // when
            val spending = service.spending(listOf(withdrawal))

            // then
            spending shouldBe Spending(cash = 0, digital = 150)
        }
    }

    private fun payment() = ActivityLine.Operation(
        at = Instant.ofEpochSecond(1_000),
        transactionId = 1,
        type = PendingType.EXPENSE,
        status = ActivityStatus.CONFIRMED,
        amount = 5_000,
        accountName = "Cash",
        accountKind = AccountKind.CASH,
        toAccountName = null,
        toAccountKind = null,
        categoryName = "Déplacement",
        parts = 1,
        counterparty = "BOULANGERIE SAKER",
        note = null,
        fees = 0,
        fee = false,
        reconciled = false,
    )
}
