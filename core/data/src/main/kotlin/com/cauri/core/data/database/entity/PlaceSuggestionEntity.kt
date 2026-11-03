package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A place the user often stops at, not declared: Cauri asks once whether it is a shop.
 *
 * @property id Generated identifier.
 * @property latitude Latitude of the centre of the stops, in degrees.
 * @property longitude Longitude of the centre of the stops, in degrees.
 * @property area Neighbourhood, or else the town, as the geocoder named it; `null` when unknown.
 * @property stops How many stops were found there over the last 30 days when it was found.
 * @property foundAt When it was found.
 * @property notifiedAt When the notification asked about it; `null` when it was not shown.
 * @property answer The user's answer; `null` while they have not given one.
 */
@Entity(tableName = "place_suggestions")
internal data class PlaceSuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val area: String?,
    val stops: Int,
    val foundAt: Instant,
    val notifiedAt: Instant?,
    val answer: SuggestionAnswer?,
)
