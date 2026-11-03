package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cauri.core.data.database.entity.BudgetEntity
import com.cauri.core.data.service.CategorySpend
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The budgets of the categories, and the spending they are suggested from. */
@Dao
internal interface BudgetDao {
    /** Sets the budget of its category, replacing the one it had. */
    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    /** Every budget, ignored ones too, again after each change. */
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    /**
     * The expenses counted since [since] that have a category, of every account: confirmed or waiting, never set
     * aside nor merged, and the cards still at 0 left out.
     */
    @Query(
        "SELECT categoryId, amount, occurredAt FROM transactions WHERE type = 'EXPENSE' " +
            "AND status IN ('DETECTED', 'CONFIRMED') AND amount > 0 AND categoryId IS NOT NULL " +
            "AND occurredAt >= :since",
    )
    suspend fun getSpending(since: Instant): List<CategorySpend>

    /** When the first transaction counted happened; `null` before the first. */
    @Query("SELECT MIN(occurredAt) FROM transactions WHERE status IN ('DETECTED', 'CONFIRMED') AND amount > 0")
    suspend fun getFirstTransactionAt(): Instant?
}
