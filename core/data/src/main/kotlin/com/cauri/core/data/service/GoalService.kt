package com.cauri.core.data.service

import java.time.YearMonth
import java.time.ZoneId

/** The rules of the goals: when the savings should be reached. */
internal interface GoalService {
    /**
     * A cautious estimate of what is put aside a month: the 25th percentile of the income of the full months of
     * [incomes] (the current one, [now], left out, the months without income too), times [savingsRate] percent; `null`
     * before a full month of income.
     */
    fun monthlySavings(incomes: List<IncomeAt>, savingsRate: Int, now: YearMonth, zone: ZoneId): Long?

    /**
     * The month each of [left] (what each saving still needs, in their order) should be reached in, at [monthly] put
     * aside a month: the savings fill them one after the other. `null` for those reached, and for all without
     * savings to come.
     */
    fun estimates(left: List<Long>, monthly: Long?, now: YearMonth): List<YearMonth?>

    companion object {
        /** Full months of income the estimate reads at most. */
        const val MONTHS = 6L
    }
}
