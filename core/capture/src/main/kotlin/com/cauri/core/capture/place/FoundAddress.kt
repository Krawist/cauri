package com.cauri.core.capture.place

/**
 * An address found by its name, or at a position.
 *
 * @property label What the user reads: the place or street, its neighbourhood and town (« Carrefour Bastos, Bastos,
 *   Yaoundé »).
 * @property name Name the map knows for the place there (« Carrefour Bastos »); `null` when it knows only a street or
 *   a number.
 * @property area Neighbourhood, or else the town; `null` when unknown.
 * @property latitude Latitude, in degrees.
 * @property longitude Longitude, in degrees.
 */
data class FoundAddress(
    val label: String,
    val name: String?,
    val area: String?,
    val latitude: Double,
    val longitude: Double,
)
