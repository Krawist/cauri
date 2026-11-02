package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A place declared by the user or discovered from their stays.
 *
 * @property id Generated identifier.
 * @property name Name shown to the user (« Marché Mokolo »).
 * @property latitude Latitude of the centre, in degrees.
 * @property longitude Longitude of the centre, in degrees.
 * @property radiusMeters Radius of the geofence, in metres.
 * @property kind Shop, home or work.
 * @property defaultCategoryId Category proposed for an expense at this place; `null` when there is none.
 */
@Entity(
    tableName = "places",
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["defaultCategoryId"]),
    ],
    indices = [Index("defaultCategoryId")],
)
internal data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val kind: PlaceKind,
    val defaultCategoryId: Long?,
)
