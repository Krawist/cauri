package com.cauri.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cauri.core.data.model.PlaceKind

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
 * @property remindAfterVisit Whether a visit to this shop may bring a reminder; home and work never do.
 * @property area Neighbourhood, or else the town, as the geocoder named it; `null` when unknown.
 * @property address Address of the place, as the geocoder wrote it; `null` when unknown.
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
    @ColumnInfo(defaultValue = "1") val remindAfterVisit: Boolean = true,
    val area: String? = null,
    val address: String? = null,
)
