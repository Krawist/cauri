package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.RawSignalEntity
import java.time.Instant

@Dao
internal interface RawSignalDao {
    /** Inserts [signal] and returns its generated id. */
    @Insert
    suspend fun insert(signal: RawSignalEntity): Long

    @Update
    suspend fun update(signal: RawSignalEntity)

    /** Whether the same notification ([sender], [title], [text]) was stored after [after]. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM raw_signals WHERE sender = :sender AND title IS :title AND text = :text " +
            "AND receivedAt > :after)",
    )
    suspend fun existsSince(sender: String, title: String?, text: String, after: Instant): Boolean

    /** When the last signal was received; `null` while none was kept. */
    @Query("SELECT MAX(receivedAt) FROM raw_signals")
    suspend fun getLastReceivedAt(): Instant?

    /** The signals no template recognised yet, oldest first, to replay when a template is added. */
    @Query("SELECT * FROM raw_signals WHERE templateVersion IS NULL ORDER BY receivedAt")
    suspend fun getUnparsed(): List<RawSignalEntity>

    /**
     * The signals no template recognised that the phone's model was never asked about, received since [since], the
     * oldest first, [limit] at most.
     */
    @Query(
        "SELECT * FROM raw_signals WHERE templateVersion IS NULL AND modelReadAt IS NULL AND receivedAt >= :since " +
            "ORDER BY receivedAt LIMIT :limit",
    )
    suspend fun getUnreadByModel(since: Instant, limit: Int): List<RawSignalEntity>

    /** Remembers that the phone's model was asked about the signal [id] at [at]. */
    @Query("UPDATE raw_signals SET modelReadAt = :at WHERE id = :id")
    suspend fun setModelReadAt(id: Long, at: Instant)
}
