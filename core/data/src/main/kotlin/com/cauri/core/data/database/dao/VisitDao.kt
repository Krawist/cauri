package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.VisitEntity
import java.time.Instant

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

    /** Whether the user is still in the place [placeId], a visit there not left yet. */
    @Query("SELECT EXISTS(SELECT 1 FROM visits WHERE placeId = :placeId AND leftAt IS NULL)")
    suspend fun isOngoing(placeId: Long): Boolean

    /**
     * Opens a visit to the place [placeId] from [enteredAt], unless one is open there already: Android may tell the
     * same stay twice, such as after a restart.
     */
    @Transaction
    suspend fun enter(placeId: Long, enteredAt: Instant) {
        if (!isOngoing(placeId)) insert(VisitEntity(placeId = placeId, enteredAt = enteredAt, leftAt = null))
    }

    /** Closes at [leftAt] the visit to the place [placeId] not left yet; nothing when there is none. */
    @Query("UPDATE visits SET leftAt = :leftAt WHERE placeId = :placeId AND leftAt IS NULL")
    suspend fun leave(placeId: Long, leftAt: Instant)
}
