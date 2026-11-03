package com.cauri.core.data.service

import java.time.Duration
import java.time.Instant

/** The rules of the budgets: what Cauri suggests for each category from what was really spent. */
internal interface BudgetService {
    /**
     * The budget a month suggested for each category of [spending], by category: nothing before [LEARNING] of history
     * since [firstTransactionAt]; then what each category cost over the last [WINDOW] at most (the history when
     * shorter), brought to 30 days and rounded up to 500 F, for the categories spent on twice at least.
     */
    fun suggest(spending: List<CategorySpend>, firstTransactionAt: Instant?, now: Instant): Map<Long, Long>

    companion object {
        /** History before the first suggestions: four weeks of real spending. */
        val LEARNING: Duration = Duration.ofDays(28)

        /** Spending read for the suggestions: six weeks at most. */
        val WINDOW: Duration = Duration.ofDays(42)
    }
}
