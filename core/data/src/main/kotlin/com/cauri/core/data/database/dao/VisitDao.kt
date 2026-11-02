package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.VisitEntity

@Dao
internal interface VisitDao {
    /** Inserts [visit] and returns its generated id. */
    @Insert
    suspend fun insert(visit: VisitEntity): Long

    @Update
    suspend fun update(visit: VisitEntity)

    /** The visits the user has not left yet. */
    @Query("SELECT * FROM visits WHERE leftAt IS NULL")
    suspend fun getOngoing(): List<VisitEntity>
}
