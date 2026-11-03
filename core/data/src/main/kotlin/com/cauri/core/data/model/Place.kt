package com.cauri.core.data.model

/**
 * A place the user declared: a shop to be reminded about after a visit, or home and work, which never bring one.
 *
 * @property id Identifier of the place.
 * @property name Name shown to the user (« Marché Mokolo »).
 * @property latitude Latitude of the centre, in degrees.
 * @property longitude Longitude of the centre, in degrees.
 * @property radiusMeters Radius of the area around the centre, in metres.
 * @property kind Shop, home or work.
 * @property defaultCategoryId Category proposed for an expense there; `null` when none.
 * @property remindAfterVisit Whether a visit to this shop may bring a reminder; always `false` for home and work.
 * @property visits How many visits were detected there.
 * @property area Neighbourhood, or else the town (« Bastos »); `null` when unknown.
 * @property address Address of the place (« Carrefour Bastos, Bastos, Yaoundé »); `null` when unknown.
 */
data class Place(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val kind: PlaceKind,
    val defaultCategoryId: Long?,
    val remindAfterVisit: Boolean,
    val area: String?,
    val address: String?,
    val visits: Int,
)
