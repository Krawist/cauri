package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.FixedCostEntity
import com.cauri.core.data.database.entity.GoalContributionEntity
import com.cauri.core.data.database.entity.GoalEntity
import com.cauri.core.data.database.entity.IncomeSplitEntity
import com.cauri.core.data.service.GoalTotal
import com.cauri.core.data.service.IncomeAt
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The goals, the money put aside for them, and what they are estimated from. */
// One query per figure of « Objectifs ».
@Suppress("TooManyFunctions")
@Dao
internal interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun get(id: Long): GoalEntity?

    @Insert
    suspend fun insertFixedCost(cost: FixedCostEntity): Long

    @Update
    suspend fun updateFixedCost(cost: FixedCostEntity)

    @Query("SELECT * FROM fixed_costs WHERE id = :id")
    suspend fun getFixedCost(id: Long): FixedCostEntity?

    @Query("DELETE FROM fixed_costs WHERE id = :id")
    suspend fun deleteFixedCost(id: Long)

    @Insert
    suspend fun insertContribution(contribution: GoalContributionEntity)

    /** The goals in their order, with the day of a tontine's contribution, again after each change. */
    @Query(
        "SELECT goals.*, fixed_costs.dayOfMonth FROM goals " +
            "LEFT JOIN fixed_costs ON fixed_costs.id = goals.fixedCostId ORDER BY position, goals.id",
    )
    fun observeAll(): Flow<List<GoalRow>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM goals")
    suspend fun getNextPosition(): Int

    /** Puts the goal [id] first, the others after it in their order. */
    @Query("UPDATE goals SET position = CASE WHEN id = :id THEN 0 ELSE position + 1 END")
    suspend fun moveFirst(id: Long)

    /** What each saving holds, again after each change. */
    @Query("SELECT goalId, SUM(amount) AS amount FROM goal_contributions GROUP BY goalId")
    fun observeSaved(): Flow<List<GoalTotal>>

    /** What was put aside since [since], what was taken back left out, again after each change. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM goal_contributions WHERE amount > 0 AND at >= :since")
    fun observePutAsideSince(since: Instant): Flow<Long>

    /** What each envelope's category spent since [since]: the expenses confirmed or waiting. */
    @Query(
        "SELECT goals.id AS goalId, COALESCE(SUM(transactions.amount), 0) AS amount FROM goals " +
            "JOIN transactions ON transactions.categoryId = goals.categoryId " +
            "WHERE goals.kind = 'ENVELOPE' AND transactions.type = 'EXPENSE' " +
            "AND transactions.status IN ('DETECTED', 'CONFIRMED') AND transactions.occurredAt >= :since " +
            "GROUP BY goals.id",
    )
    fun observeEnvelopesSpent(since: Instant): Flow<List<GoalTotal>>

    /** The incomes arrived since [since], the money a cash count found left out, again after each change. */
    @Query(
        "SELECT amount, occurredAt FROM transactions WHERE type = 'INCOME' " +
            "AND status IN ('DETECTED', 'CONFIRMED') AND origin != 'RECONCILIATION' AND occurredAt >= :since",
    )
    fun observeIncomes(since: Instant): Flow<List<IncomeAt>>

    /** The saving the next savings go to: the first, in order, that holds less than its amount; `null` if none. */
    @Query(
        "SELECT goals.id FROM goals WHERE kind = 'SAVING' AND amount > " +
            "(SELECT COALESCE(SUM(amount), 0) FROM goal_contributions WHERE goalId = goals.id) " +
            "ORDER BY position, id LIMIT 1",
    )
    suspend fun getReceiving(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: IncomeSplitEntity)

    /** Keeps the split of an income and puts its savings aside for the saving they go to, if any, all or none. */
    @Transaction
    suspend fun split(split: IncomeSplitEntity) {
        insertSplit(split)
        val goalId = getReceiving()
        if (split.savings > 0 && goalId != null) {
            insertContribution(
                GoalContributionEntity(
                    goalId = goalId,
                    amount = split.savings,
                    at = split.splitAt,
                    transactionId = split.transactionId,
                ),
            )
        }
    }

    /** Saves a tontine and the fixed cost of its contribution, all or none. */
    @Transaction
    suspend fun insertTontine(goal: GoalEntity, cost: FixedCostEntity): Long =
        insert(goal.copy(fixedCostId = insertFixedCost(cost)))

    /** Deletes a goal and the fixed cost of its contribution, if a tontine, all or none. */
    @Transaction
    suspend fun deleteWithFixedCost(goal: GoalEntity) {
        delete(goal.id)
        goal.fixedCostId?.let { deleteFixedCost(it) }
    }
}
