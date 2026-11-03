package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlaceDao {
    /** Inserts [place] and returns its generated id. */
    @Insert
    suspend fun insert(place: PlaceEntity): Long

    @Update
    suspend fun update(place: PlaceEntity)

    @Query("SELECT * FROM places ORDER BY name")
    fun observeAll(): Flow<List<PlaceEntity>>

    /** The places by name, each with its number of visits, again after each change of either. */
    @Query(
        "SELECT places.*, (SELECT COUNT(*) FROM visits WHERE visits.placeId = places.id) AS visits " +
            "FROM places ORDER BY name COLLATE NOCASE",
    )
    fun observeWithVisits(): Flow<List<PlaceVisits>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Long): PlaceEntity?

    /** Deletes the place [id] and its visits; the transactions made there keep their other values. */
    @Transaction
    suspend fun delete(id: Long) {
        clearTransactions(id)
        deletePlace(id)
    }

    @Query("UPDATE transactions SET placeId = NULL WHERE placeId = :id")
    suspend fun clearTransactions(id: Long)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlace(id: Long)
}
