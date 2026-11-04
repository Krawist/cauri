package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.service.CategoryTotal
import com.cauri.core.data.service.CategoryUse
import java.time.Instant
import kotlinx.coroutines.flow.Flow

// One query per change a category goes through, a merge touching every table that names it.
@Suppress("TooManyFunctions")
@Dao
internal interface CategoryDao {
    /** Inserts [category] and returns its generated id. */
    @Insert
    suspend fun insert(category: CategoryEntity): Long

    /** Inserts [categories] in one statement, all or none, and returns their generated ids in the same order. */
    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * The categories chosen for the confirmed transactions of [type] on [accountId] since [since]: what the suggestions
     * learn from.
     */
    @Query(
        "SELECT categoryId, occurredAt FROM transactions WHERE accountId = :accountId AND type = :type " +
            "AND status = 'CONFIRMED' AND categoryId IS NOT NULL AND occurredAt >= :since",
    )
    suspend fun getUses(accountId: Long, type: TransactionType, since: Instant): List<CategoryUse>

    /**
     * The categories chosen for the confirmed transactions of [type] with the merchant [counterparty], case and spaces
     * around ignored, since [since].
     */
    @Query(
        "SELECT categoryId, occurredAt FROM transactions " +
            "WHERE LOWER(TRIM(counterparty)) = LOWER(TRIM(:counterparty)) AND type = :type " +
            "AND status = 'CONFIRMED' AND categoryId IS NOT NULL AND occurredAt >= :since",
    )
    suspend fun getUsesWith(counterparty: String, type: TransactionType, since: Instant): List<CategoryUse>

    /** The categories chosen for the confirmed transactions of [type] at the place [placeId] since [since]. */
    @Query(
        "SELECT categoryId, occurredAt FROM transactions WHERE placeId = :placeId AND type = :type " +
            "AND status = 'CONFIRMED' AND categoryId IS NOT NULL AND occurredAt >= :since",
    )
    suspend fun getUsesAt(placeId: Long, type: TransactionType, since: Instant): List<CategoryUse>

    /** Every category, hidden ones too. */
    @Query("SELECT * FROM categories")
    fun observeAll(): Flow<List<CategoryEntity>>

    /** The active categories, sub-categories included, in the user's order, then by name. */
    @Query("SELECT * FROM categories WHERE status = 'ACTIVE' ORDER BY position, name")
    fun observeActive(): Flow<List<CategoryEntity>>

    /** The archived categories, by name. */
    @Query("SELECT * FROM categories WHERE status = 'ARCHIVED' ORDER BY name")
    fun observeArchived(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun get(id: Long): CategoryEntity?

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM categories")
    suspend fun getNextPosition(): Int

    @Query("UPDATE categories SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    /** Archives or brings back the category [id] and its sub-categories. */
    @Query("UPDATE categories SET status = :status WHERE id = :id OR parentId = :id")
    suspend fun setStatus(id: Long, status: CategoryStatus)

    /** Puts the categories in the order of [ids], all or none. */
    @Transaction
    suspend fun reorder(ids: List<Long>) = ids.forEachIndexed { position, id -> setPosition(id, position) }

    /**
     * What each category counted since [since], again after each change: the expenses and the incomes confirmed or
     * waiting, never set aside nor merged.
     */
    @Query(
        "SELECT categoryId, SUM(amount) AS amount FROM transactions WHERE type IN ('EXPENSE', 'INCOME') " +
            "AND status IN ('DETECTED', 'CONFIRMED') AND amount > 0 AND categoryId IS NOT NULL " +
            "AND occurredAt >= :since GROUP BY categoryId",
    )
    fun observeTotals(since: Instant): Flow<List<CategoryTotal>>

    @Query("UPDATE transactions SET categoryId = :into WHERE categoryId = :from")
    suspend fun moveTransactions(from: Long, into: Long)

    @Query("UPDATE transactions SET proposedCategoryId = :into WHERE proposedCategoryId = :from")
    suspend fun moveProposals(from: Long, into: Long)

    @Query("UPDATE transaction_merges SET keptCategoryId = :into WHERE keptCategoryId = :from")
    suspend fun moveMerges(from: Long, into: Long)

    @Query("UPDATE categories SET parentId = :into WHERE parentId = :from")
    suspend fun moveChildren(from: Long, into: Long)

    @Query("UPDATE habits SET categoryId = :into WHERE categoryId = :from")
    suspend fun moveHabits(from: Long, into: Long)

    @Query("UPDATE places SET defaultCategoryId = :into WHERE defaultCategoryId = :from")
    suspend fun movePlaces(from: Long, into: Long)

    @Query("UPDATE fixed_costs SET categoryId = :into WHERE categoryId = :from")
    suspend fun moveFixedCosts(from: Long, into: Long)

    @Query("UPDATE goals SET categoryId = :into WHERE categoryId = :from")
    suspend fun moveGoals(from: Long, into: Long)

    @Query("DELETE FROM budgets WHERE categoryId = :id")
    suspend fun deleteBudget(id: Long)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * Moves everything of the category [from] into [into], all or none, then deletes it: its transactions and
     * proposals, its habits, places, fixed costs and envelopes, and its sub-categories, which go under [intoTop], the
     * top-level category of [into]; its budget goes, [into] keeping its own.
     */
    @Transaction
    suspend fun merge(from: Long, into: Long, intoTop: Long) {
        moveTransactions(from, into)
        moveProposals(from, into)
        moveMerges(from, into)
        moveChildren(from, intoTop)
        moveHabits(from, into)
        movePlaces(from, into)
        moveFixedCosts(from, into)
        moveGoals(from, into)
        deleteBudget(from)
        delete(from)
    }
}
