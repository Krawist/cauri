package com.cauri.core.data.service

import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class FixedCostServiceImplTest {
    private val service = FixedCostServiceImpl()

    @Nested
    inner class Suggest {
        @Test
        fun `should suggest a merchant paid once a month, with the median amount and the latest day`() {
            // given
            val expenses = listOf(
                spend(counterparty = "ENEO", amount = 12_000, at = "2026-08-05T10:00:00Z"),
                spend(counterparty = "eneo ", amount = 11_000, at = "2026-09-06T10:00:00Z"),
                spend(counterparty = "ENEO", amount = 12_500, at = "2026-10-07T10:00:00Z"),
            )

            // when
            val suggestions = service.suggest(expenses, zone = ZoneOffset.UTC)

            // then
            suggestions shouldContainExactly listOf(
                FixedCostSuggestion(
                    cost = NewFixedCost(
                        name = "ENEO",
                        amount = 12_000,
                        dayOfMonth = 7,
                        categoryId = 3,
                        counterparty = "ENEO",
                    ),
                    months = 3,
                ),
            )
        }

        @Test
        fun `should suggest an amount typed every month in the same category, named after it`() {
            // given
            val expenses = listOf(
                spend(counterparty = null, amount = 50_000, at = "2026-09-01T10:00:00Z"),
                spend(counterparty = null, amount = 50_000, at = "2026-10-02T10:00:00Z"),
            )

            // when
            val suggestions = service.suggest(expenses, zone = ZoneOffset.UTC)

            // then
            suggestions shouldContainExactly listOf(
                FixedCostSuggestion(
                    cost = NewFixedCost(name = "Logement", amount = 50_000, dayOfMonth = 2, categoryId = 3),
                    months = 2,
                ),
            )
        }

        @Test
        fun `should not suggest a merchant paid in one month only`() {
            // given
            val expenses = listOf(spend(counterparty = "ENEO", amount = 12_000, at = "2026-10-05T10:00:00Z"))

            // when & then
            service.suggest(expenses, zone = ZoneOffset.UTC).shouldBeEmpty()
        }

        @Test
        fun `should not suggest a merchant paid several times in a month`() {
            // given
            val expenses = listOf(
                spend(counterparty = "Boulangerie Saker", amount = 2_000, at = "2026-09-05T10:00:00Z"),
                spend(counterparty = "Boulangerie Saker", amount = 2_000, at = "2026-10-05T10:00:00Z"),
                spend(counterparty = "Boulangerie Saker", amount = 2_000, at = "2026-10-12T10:00:00Z"),
            )

            // when & then
            service.suggest(expenses, zone = ZoneOffset.UTC).shouldBeEmpty()
        }

        @Test
        fun `should not suggest a merchant whose amounts vary by more than 10 percent`() {
            // given
            val expenses = listOf(
                spend(counterparty = "Total", amount = 10_000, at = "2026-09-05T10:00:00Z"),
                spend(counterparty = "Total", amount = 15_000, at = "2026-10-05T10:00:00Z"),
            )

            // when & then
            service.suggest(expenses, zone = ZoneOffset.UTC).shouldBeEmpty()
        }
    }

    @Nested
    inner class Pays {
        @Test
        fun `should be paid by an expense of its merchant`() {
            // given
            val cost = NewFixedCost(name = "Électricité", amount = 12_000, dayOfMonth = 5, counterparty = "ENEO")

            // when & then
            service.pays(spend(counterparty = "eneo", amount = 9_000), cost).shouldBeTrue()
        }

        @ParameterizedTest
        @CsvSource("Loyer, , ", ", Loyer, ", ", , loyer")
        fun `should be paid by an expense named as it`(counterparty: String?, note: String?, category: String?) {
            // given
            val cost = NewFixedCost(name = "Loyer", amount = 50_000, dayOfMonth = 1)

            // when & then
            service.pays(
                spend(counterparty = counterparty, note = note, categoryName = category, amount = 50_000),
                cost,
            ).shouldBeTrue()
        }

        @ParameterizedTest
        @CsvSource("45000, true", "44000, false")
        fun `should be paid by an expense of its category for about its amount`(amount: Long, paid: Boolean) {
            // given
            val cost = NewFixedCost(name = "Scolarité", amount = 50_000, dayOfMonth = 1, categoryId = 3)

            // when & then
            service.pays(spend(counterparty = null, amount = amount), cost) shouldBe paid
        }
    }

    @Nested
    inner class Same {
        @Test
        fun `should know a merchant again, case ignored`() {
            // given
            val suggested = NewFixedCost(name = "ENEO", amount = 12_000, dayOfMonth = 5, counterparty = "ENEO")
            val known = NewFixedCost(name = "Électricité", amount = 15_000, dayOfMonth = 1, counterparty = "Eneo")

            // when & then
            service.same(suggested, known).shouldBeTrue()
        }

        @Test
        fun `should tell apart two amounts of a category`() {
            // given
            val suggested = NewFixedCost(name = "Logement", amount = 50_000, dayOfMonth = 1, categoryId = 3)
            val known = NewFixedCost(name = "Loyer", amount = 80_000, dayOfMonth = 1, categoryId = 3)

            // when & then
            service.same(suggested, known).shouldBeFalse()
        }
    }

    private fun spend(
        counterparty: String?,
        amount: Long,
        at: String = "2026-11-02T10:00:00Z",
        note: String? = null,
        categoryName: String? = "Logement",
    ) = FixedCostSpend(
        categoryId = 3,
        categoryName = categoryName,
        counterparty = counterparty,
        note = note,
        amount = amount,
        occurredAt = Instant.parse(at),
    )
}
