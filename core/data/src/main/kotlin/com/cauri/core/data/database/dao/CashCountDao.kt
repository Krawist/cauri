package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cauri.core.data.database.entity.CashCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CashCountDao {
    /** Inserts [count] and returns its generated id. */
    @Insert
    suspend fun insert(count: CashCountEntity): Long

    /** The most recent count; `null` before the first one. */
    @Query("SELECT * FROM cash_counts ORDER BY countedAt DESC LIMIT 1")
    suspend fun getLatest(): CashCountEntity?

    /** The most recent count, again after each new one; `null` before the first one. */
    @Query("SELECT * FROM cash_counts ORDER BY countedAt DESC LIMIT 1")
    fun observeLatest(): Flow<CashCountEntity?>
}
