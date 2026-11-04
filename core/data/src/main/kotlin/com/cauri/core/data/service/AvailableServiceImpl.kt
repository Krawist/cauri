package com.cauri.core.data.service

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [AvailableService]. */
@Singleton
internal class AvailableServiceImpl @Inject constructor() : AvailableService {
    override fun perDay(month: MonthMoney, today: LocalDate): Long {
        val left = (month.received - month.savings - month.unpaidFixedCosts - month.spent).coerceAtLeast(0)
        val days = ChronoUnit.DAYS.between(today, today.with(TemporalAdjusters.lastDayOfMonth())) + 1
        return left / days / STEP * STEP
    }

    private companion object {
        const val STEP = 100L
    }
}
