package com.cauri.core.data.service

import com.cauri.core.data.model.IncomeSplit

/** The rules of the split of an income: savings first, then the fixed costs, then the budgets. */
internal interface IncomeSplitService {
    /**
     * The split of an income of [amount]: [savingsRate] percent of it put aside, rounded down to 100 F; then what the
     * [fixedCosts] of the month not paid yet still need, as far as the income goes (what earlier incomes kept for the
     * fixed costs pays those paid since first, and the rest of it the others); the rest to the budgets. Amounts in
     * minor units of XAF.
     */
    fun propose(amount: Long, savingsRate: Int, fixedCosts: FixedCostTotals): IncomeSplit
}
