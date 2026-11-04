package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cauri.core.data.database.entity.IncomeSplitEntity
import com.cauri.core.data.model.PendingIncome
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** How the incomes were split. */
@Dao
internal interface IncomeSplitDao {
    /** Keeps the answer about an income, replacing one given before. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(split: IncomeSplitEntity)

    /**
     * The incomes of [threshold] or more arrived since [since], newest first, never answered: confirmed or waiting,
     * the money a cash count found left out.
     */
    @Query(
        "SELECT transactions.id AS transactionId, amount, accounts.name AS accountName, counterparty, occurredAt " +
            "FROM transactions JOIN accounts ON accounts.id = transactions.accountId " +
            "WHERE type = 'INCOME' AND transactions.status IN ('DETECTED', 'CONFIRMED') " +
            "AND origin != 'RECONCILIATION' AND amount >= :threshold AND occurredAt >= :since " +
            "AND transactions.id NOT IN (SELECT transactionId FROM income_splits) ORDER BY occurredAt DESC",
    )
    fun observePending(threshold: Long, since: Instant): Flow<List<PendingIncome>>

    /** The incomes split since [since], again after each change; those not split left out. */
    @Query(
        "SELECT income_splits.* FROM income_splits " +
            "JOIN transactions ON transactions.id = income_splits.transactionId " +
            "WHERE dismissed = 0 AND occurredAt >= :since",
    )
    fun observeSplits(since: Instant): Flow<List<IncomeSplitEntity>>
}
