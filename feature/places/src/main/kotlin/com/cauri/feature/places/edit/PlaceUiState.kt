package com.cauri.feature.places.edit

import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.PlaceRepository

/**
 * State of the screen of a place.
 *
 * @property editing Whether it changes a place saved before, rather than adding one.
 * @property loaded Whether the place changed was read; always `true` for a new one.
 * @property name Name typed.
 * @property kind Shop, home or work.
 * @property categories The expense categories, by name.
 * @property categoryId Default category chosen; `null` for none.
 * @property radiusMeters Radius chosen, in metres.
 * @property remindAfterVisit Whether a visit to the shop may bring a reminder.
 * @property latitude Latitude of the place, in degrees; `null` until the phone was located.
 * @property longitude Longitude of the place, in degrees; `null` until the phone was located.
 * @property accuracyMeters How precise the position just found is, in metres; `null` for the position saved before
 *   or none yet.
 * @property locating Whether the phone is being located.
 * @property notFound Whether the last try found no position.
 * @property area Neighbourhood, or else the town, of the position; `null` when unknown.
 * @property address Address of the position, found by the search or at the position of the phone; `null` when
 *   unknown.
 * @property query Text of the address field: the [address], or what the user types to search another one.
 * @property results Addresses found for [query], the best first.
 * @property searching Whether the addresses of [query] are being looked for.
 * @property searched Whether the search of [query] ended, so that no [results] means none was found.
 * @property confirmingDelete Whether the dialog asking to delete the place is open.
 * @property canSave Whether « Enregistrer le lieu » can save: a name and a position.
 * @property closed Whether the place was saved or deleted, and the screen closes.
 */
internal data class PlaceUiState(
    val editing: Boolean = false,
    val loaded: Boolean = true,
    val name: String = "",
    val kind: PlaceKind = PlaceKind.SHOP,
    val categories: List<Category> = emptyList(),
    val categoryId: Long? = null,
    val radiusMeters: Int = PlaceRepository.DEFAULT_RADIUS_METERS,
    val remindAfterVisit: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Int? = null,
    val locating: Boolean = false,
    val notFound: Boolean = false,
    val area: String? = null,
    val address: String? = null,
    val query: String = "",
    val results: List<FoundAddress> = emptyList(),
    val searching: Boolean = false,
    val searched: Boolean = false,
    val confirmingDelete: Boolean = false,
    val canSave: Boolean = false,
    val closed: Boolean = false,
)
