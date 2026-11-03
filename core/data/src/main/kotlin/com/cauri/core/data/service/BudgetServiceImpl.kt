package com.cauri.core.data.service

import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [BudgetService]. */
@Singleton
internal class BudgetServiceImpl @Inject constructor() : BudgetService {
    override fun suggest(spending: List<CategorySpend>, firstTransactionAt: Instant?, now: Instant): Map<Long, Long> {
        val history = firstTransactionAt?.let { Duration.between(it, now) }
        if (history == null || history < BudgetService.LEARNING) return emptyMap()
        val window = minOf(history, BudgetService.WINDOW)
        val days = window.toDays().toDouble()
        return spending.filter { !it.occurredAt.isBefore(now - window) }
            .groupBy { it.categoryId }
            .filterValues { it.size >= MIN_EXPENSES }
            .mapValues { (_, expenses) -> roundUp(expenses.sumOf { it.amount } * DAYS_PER_MONTH / days) }
    }

    // Up to the next 500 F: a budget reads round.
    private fun roundUp(amount: Double): Long = Math.ceil(amount / STEP).toLong() * STEP

    private companion object {
        const val MIN_EXPENSES = 2
        const val DAYS_PER_MONTH = 30.0
        const val STEP = 500L
    }
}
