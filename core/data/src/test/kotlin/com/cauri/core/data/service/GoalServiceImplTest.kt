package com.cauri.core.data.service

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GoalServiceImplTest {
    private val service = GoalServiceImpl()

    @Nested
    inner class MonthlySavings {
        @Test
        fun `should take the lower quartile of the full months of income, times the savings rate`() {
            // given
            val incomes = listOf(
                income(100_000, "2026-07-05"),
                income(300_000, "2026-08-05"),
                income(150_000, "2026-09-05"),
                income(50_000, "2026-09-20"),
                income(250_000, "2026-10-05"),
                income(900_000, "2026-11-02"),
            )

            // when
            val monthly = service.monthlySavings(incomes, savingsRate = 10, now = NOW, zone = ZoneOffset.UTC)

            // then
            monthly shouldBe 10_000
        }

        @Test
        fun `should know nothing before a full month of income`() {
            // given
            val incomes = listOf(income(900_000, "2026-11-02"))

            // when & then
            service.monthlySavings(incomes, savingsRate = 10, now = NOW, zone = ZoneOffset.UTC).shouldBeNull()
        }
    }

    @Nested
    inner class Estimates {
        @Test
        fun `should fill the savings one after the other, from next month`() {
            // when
            val estimates = service.estimates(left = listOf(25_000, 0, 10_000), monthly = 10_000, now = NOW)

            // then
            estimates shouldContainExactly listOf(YearMonth.of(2027, 2), null, YearMonth.of(2027, 3))
        }

        @Test
        fun `should estimate nothing without savings to come`() {
            // when
            val estimates = service.estimates(left = listOf(25_000), monthly = null, now = NOW)

            // then
            estimates shouldContainExactly listOf(null)
        }
    }

    private fun income(amount: Long, day: String) = IncomeAt(amount, Instant.parse("${day}T10:00:00Z"))

    private companion object {
        val NOW: YearMonth = YearMonth.of(2026, 11)
    }
}
