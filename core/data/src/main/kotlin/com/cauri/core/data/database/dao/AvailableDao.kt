package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The money of the month the daily available amount is computed from. */
@Dao
internal interface AvailableDao {
    /**
     * What arrived since [since], again after each change: the incomes confirmed or waiting, the money a cash count
     * found left out; transfers between the user's accounts are no income.
     */
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' " +
            "AND status IN ('DETECTED', 'CONFIRMED') AND origin != 'RECONCILIATION' AND occurredAt >= :since",
    )
    fun observeReceived(since: Instant): Flow<Long>

    /** What was spent since [since], again after each change: the expenses confirmed or waiting, fees included. */
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' " +
            "AND status IN ('DETECTED', 'CONFIRMED') AND occurredAt >= :since",
    )
    fun observeSpent(since: Instant): Flow<Long>
}
