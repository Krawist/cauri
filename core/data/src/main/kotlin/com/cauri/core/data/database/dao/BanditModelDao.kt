package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cauri.core.data.database.entity.BanditModelEntity

/** The models of the reminders bandit. */
@Dao
internal interface BanditModelDao {
    /** Publishes [model], in one write, and returns its version. */
    @Insert
    suspend fun insert(model: BanditModelEntity): Long

    /** The latest model; `null` before the first. */
    @Query("SELECT * FROM bandit_models ORDER BY version DESC LIMIT 1")
    suspend fun getLatest(): BanditModelEntity?

    /** Forgets every model: the rules decide again. */
    @Query("DELETE FROM bandit_models")
    suspend fun deleteAll()
}
