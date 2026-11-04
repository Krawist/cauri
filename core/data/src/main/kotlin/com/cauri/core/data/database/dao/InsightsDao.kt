package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cauri.core.data.service.HabitUse
import com.cauri.core.data.service.MonthExpense
import java.time.Instant

/** What « Analyses » reads of the transactions. */
@Dao
internal interface InsightsDao {
    /**
     * The expenses counted from [from] to [to] excluded: confirmed or waiting, never set aside nor merged, the cards
     * still at 0 left out; each with its category (a sub-category's parent), whether it was cash, and whether it is
     * the rest of a count.
     */
    @Query(
        "SELECT COALESCE(categories.parentId, transactions.categoryId) AS categoryId, " +
            "transactions.amount AS amount, accounts.kind = 'CASH' AS cash, " +
            "(transactions.origin = 'RECONCILIATION' AND transactions.categoryId IS NULL) AS unidentified " +
            "FROM transactions JOIN accounts ON accounts.id = transactions.accountId " +
            "LEFT JOIN categories ON categories.id = transactions.categoryId " +
            "WHERE transactions.type = 'EXPENSE' AND transactions.status IN ('DETECTED', 'CONFIRMED') " +
            "AND transactions.amount > 0 AND transactions.occurredAt >= :from AND transactions.occurredAt < :to",
    )
    suspend fun getExpenses(from: Instant, to: Instant): List<MonthExpense>

    /** The cash expenses confirmed since [since] named after one of [names]: the taps on those habits. */
    @Query(
        "SELECT note, amount FROM transactions WHERE accountId = 1 AND type = 'EXPENSE' AND status = 'CONFIRMED' " +
            "AND note IN (:names) AND occurredAt >= :since",
    )
    suspend fun getHabitUses(names: List<String>, since: Instant): List<HabitUse>
}
