package com.cauri.core.data.model

/**
 * A place the user often stops at, not declared, that Cauri asks about: « Tu viens souvent ici. C'est un commerce ? ».
 *
 * @property id Identifier of the suggestion.
 * @property latitude Latitude of the place, in degrees.
 * @property longitude Longitude of the place, in degrees.
 * @property area Neighbourhood, or else the town (« Bastos »); `null` when unknown.
 * @property stops How many stops the user made there over the last 30 days.
 * @property notified Whether a notification asked about it already: it asks once.
 */
data class PlaceSuggestion(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val area: String?,
    val stops: Int,
    val notified: Boolean,
)
