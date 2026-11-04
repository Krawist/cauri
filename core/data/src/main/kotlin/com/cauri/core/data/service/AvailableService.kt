package com.cauri.core.data.service

import java.time.LocalDate

/** The rule of the daily available amount. */
internal interface AvailableService {
    /**
     * What can be spent each day until the end of the month of [today], today included: the money of the [month]
     * received, minus the savings put aside, the fixed costs not paid yet and what was spent, never below 0, rounded
     * down to 100 F. Amounts in minor units of XAF.
     */
    fun perDay(month: MonthMoney, today: LocalDate): Long
}
