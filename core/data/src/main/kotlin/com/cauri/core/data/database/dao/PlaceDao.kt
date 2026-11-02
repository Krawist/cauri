package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
