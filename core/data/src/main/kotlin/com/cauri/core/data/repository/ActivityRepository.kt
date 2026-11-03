package com.cauri.core.data.repository

import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.MonthSpending
import kotlinx.coroutines.flow.Flow

/** « Activité »: the history of the money on the user's accounts. */
interface ActivityRepository {
    /** Every operation and cash count, newest first; a transfer and a split operation are one line each. */
    val lines: Flow<List<ActivityLine>>

    /** What the user spent since the first day of the current month, cash apart from digital. */
    val monthSpending: Flow<MonthSpending>
}
