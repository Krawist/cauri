package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.RawSignalEntity

@Dao
internal interface RawSignalDao {
    /** Inserts [signal] and returns its generated id. */
    @Insert
    suspend fun insert(signal: RawSignalEntity): Long

    @Update
    suspend fun update(signal: RawSignalEntity)

    /** The signals no template recognised yet, oldest first, to replay when a template is added. */
    @Query("SELECT * FROM raw_signals WHERE templateVersion IS NULL ORDER BY receivedAt")
    suspend fun getUnparsed(): List<RawSignalEntity>
}
