package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.FixedCostEntity
import com.cauri.core.data.service.FixedCostSpend
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The fixed costs, and the expenses they are found and recognised from. */
@Dao
internal interface FixedCostDao {
    @Insert
    suspend fun insert(cost: FixedCostEntity): Long

    @Update
    suspend fun update(cost: FixedCostEntity)

    @Query("DELETE FROM fixed_costs WHERE id = :id")
    suspend fun delete(id: Long)

    /** Every fixed cost, refused ones too, again after each change. */
    @Query("SELECT * FROM fixed_costs ORDER BY dayOfMonth, name")
    fun observeAll(): Flow<List<FixedCostEntity>>

    /** The expenses counted since [since], with the name of their category: confirmed or waiting, never at 0. */
    @Query(
        "SELECT transactions.categoryId, categories.name AS categoryName, counterparty, note, amount, occurredAt " +
            "FROM transactions LEFT JOIN categories ON categories.id = transactions.categoryId " +
            "WHERE type = 'EXPENSE' AND transactions.status IN ('DETECTED', 'CONFIRMED') AND amount > 0 " +
            "AND occurredAt >= :since",
    )
    fun observeExpenses(since: Instant): Flow<List<FixedCostSpend>>
}
