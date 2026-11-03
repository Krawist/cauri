package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.cauri.core.data.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HabitDao {
    /** Inserts [habits] in one statement: all or none. */
    @Insert
    suspend fun insertAll(habits: List<HabitEntity>)

    /** Changes the name, amount and category of a habit; its position stays. */
    @Query("UPDATE habits SET name = :name, amount = :amount, categoryId = :categoryId WHERE id = :id")
    suspend fun update(id: Long, name: String, amount: Long, categoryId: Long?)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: Long)

    /** Position after the last habit; 0 when there is none. */
    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM habits")
    suspend fun nextPosition(): Int

    @Query("UPDATE habits SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    /** Gives each habit of [ids] its index as position, at once. */
    @Transaction
    suspend fun reorder(ids: List<Long>) {
        ids.forEachIndexed { position, id -> updatePosition(id = id, position = position) }
    }

    /** The habits in the order of the row. */
    @Query("SELECT * FROM habits ORDER BY position")
    fun observeAll(): Flow<List<HabitEntity>>
}
