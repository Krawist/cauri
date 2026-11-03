package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.StopEntity
import com.cauri.core.data.database.entity.SuggestionAnswer
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The stops of the phone and the places Cauri suggests from them. */
// One function per statement of the writes and reads of the discovery.
@Suppress("TooManyFunctions")
@Dao
internal interface DiscoveryDao {
    /** The rows of the stops, stops or positions alone, that ended from [from] to [to], the latest first. */
    @Query("SELECT * FROM stops WHERE endedAt >= :from AND endedAt <= :to ORDER BY endedAt DESC, id DESC")
    suspend fun getEndedBetween(from: Instant, to: Instant): List<StopEntity>

    @Insert
    suspend fun insertStop(stop: StopEntity): Long

    @Update
    suspend fun updateStop(stop: StopEntity)

    /** Deletes the stops that ended before [before]: positions are kept 30 days at most. */
    @Query("DELETE FROM stops WHERE endedAt < :before")
    suspend fun deleteStopsBefore(before: Instant)

    /** Deletes the positions alone read before [before], that no position joined: only the stops are kept. */
    @Query("DELETE FROM stops WHERE positions < 2 AND endedAt < :before")
    suspend fun deleteAloneBefore(before: Instant)

    /** The stops, two positions at least, that ended from [since]. */
    @Query("SELECT * FROM stops WHERE positions >= 2 AND endedAt >= :since ORDER BY startedAt")
    suspend fun getStopsSince(since: Instant): List<StopEntity>

    /** Keeps a stop longer with [extended], or starts [started]; forgets the stops ended before [before]. */
    @Transaction
    suspend fun recordPosition(extended: StopEntity?, started: StopEntity?, before: Instant) {
        deleteStopsBefore(before)
        if (extended != null) updateStop(extended)
        if (started != null) insertStop(started)
    }

    @Query("SELECT * FROM place_suggestions")
    suspend fun getSuggestions(): List<PlaceSuggestionEntity>

    @Insert
    suspend fun insertSuggestions(suggestions: List<PlaceSuggestionEntity>): List<Long>

    @Query("SELECT * FROM place_suggestions WHERE id = :id")
    suspend fun getSuggestion(id: Long): PlaceSuggestionEntity?

    /** The suggestions not answered yet, the oldest first, again after each change. */
    @Query("SELECT * FROM place_suggestions WHERE answer IS NULL ORDER BY foundAt, id")
    fun observePending(): Flow<List<PlaceSuggestionEntity>>

    /** The suggestion not answered yet that a notification asked about last; `null` when there is none. */
    @Query(
        "SELECT * FROM place_suggestions WHERE answer IS NULL AND notifiedAt IS NOT NULL " +
            "ORDER BY notifiedAt DESC, id DESC LIMIT 1",
    )
    suspend fun getLastNotified(): PlaceSuggestionEntity?

    @Query("UPDATE place_suggestions SET area = :area WHERE id = :id")
    suspend fun setArea(id: Long, area: String?)

    @Query("UPDATE place_suggestions SET notifiedAt = :at WHERE id = :id")
    suspend fun setNotified(id: Long, at: Instant)

    @Query("UPDATE place_suggestions SET answer = :answer WHERE id = :id")
    suspend fun setAnswer(id: Long, answer: SuggestionAnswer)
}
