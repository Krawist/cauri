package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Where the phone stayed, from positions read a quarter of an hour apart, to find the places the user often stops at.
 * A row of one position is only the last one read: it becomes a stop with the next one close to it, or goes.
 *
 * @property id Generated identifier.
 * @property latitude Latitude of the first position, in degrees.
 * @property longitude Longitude of the first position, in degrees.
 * @property startedAt When the first position was read.
 * @property endedAt When the last position close to it was read.
 * @property positions How many positions were read there; from 2 it is a stop.
 */
@Entity(tableName = "stops", indices = [Index("endedAt")])
internal data class StopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val startedAt: Instant,
    val endedAt: Instant,
    val positions: Int,
)
