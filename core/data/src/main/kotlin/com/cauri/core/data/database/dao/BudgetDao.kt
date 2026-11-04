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
     * aside nor merged, and the cards still at 0 left out; a sub-category's counted in its parent.
     */
    @Query(
        "SELECT COALESCE(categories.parentId, transactions.categoryId) AS categoryId, amount, occurredAt " +
            "FROM transactions JOIN categories ON categories.id = transactions.categoryId " +
            "WHERE type = 'EXPENSE' AND transactions.status IN ('DETECTED', 'CONFIRMED') AND amount > 0 " +
            "AND occurredAt >= :since",
    )
    suspend fun getSpending(since: Instant): List<CategorySpend>

    /** When the first transaction counted happened; `null` before the first. */
    @Query("SELECT MIN(occurredAt) FROM transactions WHERE status IN ('DETECTED', 'CONFIRMED') AND amount > 0")
    suspend fun getFirstTransactionAt(): Instant?
}
