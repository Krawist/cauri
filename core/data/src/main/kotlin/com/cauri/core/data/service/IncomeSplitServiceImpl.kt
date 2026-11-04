package com.cauri.core.data.service

import com.cauri.core.data.model.IncomeSplit
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [IncomeSplitService]. */
@Singleton
internal class IncomeSplitServiceImpl @Inject constructor() : IncomeSplitService {
    override fun propose(amount: Long, savingsRate: Int, fixedCosts: FixedCostTotals): IncomeSplit {
        val savings = amount * savingsRate / PERCENT / STEP * STEP
        val keptLeft = (fixedCosts.kept - fixedCosts.paid).coerceAtLeast(0)
        val needed = (fixedCosts.unpaid - keptLeft).coerceIn(0, amount - savings)
        return IncomeSplit(savings = savings, fixedCosts = needed, budgets = amount - savings - needed)
    }

    private companion object {
        const val PERCENT = 100
        const val STEP = 100L
    }
}
