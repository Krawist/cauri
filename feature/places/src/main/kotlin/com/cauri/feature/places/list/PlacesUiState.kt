package com.cauri.feature.places.list

import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceSuggestion

/**
 * What « Mes lieux » shows.
 *
 * @property shops The shops, by name.
 * @property quiet Home and work, by name: they never bring a reminder.
 * @property categoryNames Name of each category, by id, for the shops' default category.
 * @property location How far the user lets Cauri use the location; below « always » no visit is noticed.
 * @property suggestions Places often stopped at that Cauri asks about, « Tu viens souvent ici », the oldest first.
 * @property loaded Whether the places were read once, so that an empty list means there are none.
 */
internal data class PlacesUiState(
    val shops: List<Place> = emptyList(),
    val quiet: List<Place> = emptyList(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val location: LocationAccess = LocationAccess.ALWAYS,
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val loaded: Boolean = false,
)
