package com.cauri.core.data.service

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BudgetServiceImplTest {
    private val budgets = BudgetServiceImpl()

    @Nested
    inner class Suggest {
        @Test
        fun `should suggest nothing before four weeks of history`() {
            // given
            val spending =
                listOf(
                    spend(category = 1, amount = 5_000, daysAgo = 2),
                    spend(category = 1, amount = 5_000, daysAgo = 3),
                )

            // when
            val suggested = budgets.suggest(spending, firstTransactionAt = NOW - Duration.ofDays(27), now = NOW)

            // then
            suggested.shouldBeEmpty()
        }

        @Test
        fun `should bring six weeks of spending to a month, rounded up to 500 F`() {
            // given
            val spending =
                listOf(
                    spend(category = 1, amount = 20_000, daysAgo = 10),
                    spend(category = 1, amount = 22_100, daysAgo = 30),
                )

            // when
            val suggested = budgets.suggest(spending, firstTransactionAt = NOW - Duration.ofDays(90), now = NOW)

            // then
            suggested shouldBe mapOf(1L to 30_500L)
        }

        @Test
        fun `should read only the history there is within the six weeks`() {
            // given
            val spending =
                listOf(
                    spend(category = 1, amount = 10_000, daysAgo = 5),
                    spend(category = 1, amount = 18_000, daysAgo = 20),
                )

            // when
            val suggested = budgets.suggest(spending, firstTransactionAt = NOW - Duration.ofDays(30), now = NOW)

            // then
            suggested shouldBe mapOf(1L to 28_000L)
        }

        @Test
        fun `should leave out a category spent on once`() {
            // given
            val spending = listOf(spend(category = 2, amount = 50_000, daysAgo = 3))

            // when
            val suggested = budgets.suggest(spending, firstTransactionAt = NOW - Duration.ofDays(60), now = NOW)

            // then
            suggested.shouldBeEmpty()
        }
    }

    private fun spend(category: Long, amount: Long, daysAgo: Long) =
        CategorySpend(categoryId = category, amount = amount, occurredAt = NOW - Duration.ofDays(daysAgo))

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
