package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.service.CategoryUse
import java.time.Instant
import kotlinx.coroutines.flow.Flow

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

    /** The active categories, by name. */
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

    @Query("SELECT * FROM categories WHERE status = 'ACTIVE' ORDER BY name")
    fun observeActive(): Flow<List<CategoryEntity>>
}
