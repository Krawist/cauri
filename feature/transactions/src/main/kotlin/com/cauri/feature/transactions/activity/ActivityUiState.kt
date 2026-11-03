package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.MonthSpending
import java.time.LocalDate

/**
 * State of « Activité ».
 *
 * @property filter Accounts shown.
 * @property days Days with a line on the accounts shown, newest first.
 * @property spending What was spent this month, cash and digital, whatever the filter.
 * @property today Today, which names the first days « Aujourd'hui » and « Hier »; `null` until loaded.
 */
internal data class ActivityUiState(
    val filter: ActivityFilter = ActivityFilter.ALL,
    val days: List<ActivityDay> = emptyList(),
    val spending: MonthSpending = MonthSpending(cash = 0, digital = 0),
    val today: LocalDate? = null,
)
