package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cauri.core.data.database.entity.CashCountEntity

@Dao
internal interface CashCountDao {
    /** Inserts [count] and returns its generated id. */
    @Insert
    suspend fun insert(count: CashCountEntity): Long

    /** The most recent count; `null` before the first one. */
    @Query("SELECT * FROM cash_counts ORDER BY countedAt DESC LIMIT 1")
    suspend fun getLatest(): CashCountEntity?
}
