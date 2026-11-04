package com.cauri.core.data.service

import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [GoalService]. */
@Singleton
internal class GoalServiceImpl @Inject constructor() : GoalService {
    override fun monthlySavings(incomes: List<IncomeAt>, savingsRate: Int, now: YearMonth, zone: ZoneId): Long? {
        val months = incomes.groupBy { YearMonth.from(it.occurredAt.atZone(zone)) }
            .filterKeys { it < now && it >= now.minusMonths(GoalService.MONTHS) }
            .values.map { month -> month.sumOf { it.amount } }
            .sorted()
        if (months.isEmpty()) return null
        // The lower quartile, nearest rank: one month in four brought less.
        val cautious = months[((months.size - 1) * QUARTILE).toInt()]
        return cautious * savingsRate / PERCENT
    }

    override fun estimates(left: List<Long>, monthly: Long?, now: YearMonth): List<YearMonth?> {
        var needed = 0L
        return left.map { amount ->
            if (amount <= 0 || monthly == null || monthly <= 0) {
                null
            } else {
                needed += amount
                // The savings of a month arrive with its incomes: the first ones next month.
                now.plusMonths((needed + monthly - 1) / monthly)
            }
        }
    }

    private companion object {
        const val QUARTILE = 0.25
        const val PERCENT = 100
    }
}
