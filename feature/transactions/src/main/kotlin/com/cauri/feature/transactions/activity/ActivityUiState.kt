package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivitySort
import com.cauri.core.data.model.Spending
import java.time.LocalDate

/**
 * State of « Activité ».
 *
 * @property filter Accounts shown.
 * @property query What the search looks for; `null` while the search is closed.
 * @property period Days shown.
 * @property sort Order of the lines.
 * @property days Groups of lines shown: one per day when sorted by date, newest first, one only when sorted by amount.
 * @property spending What was spent over [period], the current month for the whole history, whatever the filter and
 *   the search.
 * @property today Today, which names the first days « Aujourd'hui » and « Hier »; `null` until loaded.
 */
internal data class ActivityUiState(
    val filter: ActivityFilter = ActivityFilter.ALL,
    val query: String? = null,
    val period: ActivityPeriod = ActivityPeriod.All,
    val sort: ActivitySort = ActivitySort.DATE,
    val days: List<ActivityDay> = emptyList(),
    val spending: Spending = Spending(cash = 0, digital = 0),
    val today: LocalDate? = null,
)
