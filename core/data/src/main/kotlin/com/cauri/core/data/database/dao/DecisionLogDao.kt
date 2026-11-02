package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.DecisionLogEntity
import java.time.Instant

@Dao
internal interface DecisionLogDao {
    /** Inserts [decision] and returns its generated id. */
    @Insert
    suspend fun insert(decision: DecisionLogEntity): Long

    /** Records the outcome of a decision. */
    @Update
    suspend fun update(decision: DecisionLogEntity)

    /** The decisions taken since [from], oldest first: the training window of the bandit. */
    @Query("SELECT * FROM decision_log WHERE decidedAt >= :from ORDER BY decidedAt")
    suspend fun getSince(from: Instant): List<DecisionLogEntity>

    /** The decisions with no outcome yet. */
    @Query("SELECT * FROM decision_log WHERE outcome IS NULL ORDER BY decidedAt")
    suspend fun getOpen(): List<DecisionLogEntity>
}
