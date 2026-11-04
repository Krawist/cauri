package com.cauri.core.data.service

import com.cauri.core.data.model.IncomeSplit
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class IncomeSplitServiceImplTest {
    private val service = IncomeSplitServiceImpl()

    @Nested
    inner class Propose {
        @Test
        fun `should put the savings aside first, rounded down to 100 F, then the fixed costs`() {
            // when
            val split = service.propose(
                amount = 150_550,
                savingsRate = 10,
                fixedCosts = FixedCostTotals(unpaid = 60_000, paid = 0, kept = 0),
            )

            // then
            split shouldBe IncomeSplit(savings = 15_000, fixedCosts = 60_000, budgets = 75_550)
        }

        @Test
        fun `should keep for the fixed costs only what the income has left after the savings`() {
            // when
            val split = service.propose(
                amount = 50_000,
                savingsRate = 10,
                fixedCosts = FixedCostTotals(unpaid = 80_000, paid = 0, kept = 0),
            )

            // then
            split shouldBe IncomeSplit(savings = 5_000, fixedCosts = 45_000, budgets = 0)
        }

        @Test
        fun `should keep nothing more for the fixed costs an earlier income kept for`() {
            // when
            val split = service.propose(
                amount = 100_000,
                savingsRate = 0,
                fixedCosts = FixedCostTotals(unpaid = 60_000, paid = 0, kept = 60_000),
            )

            // then
            split shouldBe IncomeSplit(savings = 0, fixedCosts = 0, budgets = 100_000)
        }

        @Test
        fun `should count what was kept for the fixed costs paid since as spent`() {
            // when
            val split = service.propose(
                amount = 100_000,
                savingsRate = 0,
                fixedCosts = FixedCostTotals(unpaid = 30_000, paid = 50_000, kept = 60_000),
            )

            // then
            split shouldBe IncomeSplit(savings = 0, fixedCosts = 20_000, budgets = 80_000)
        }
    }
}
