package com.cauri.feature.places.discovery

import android.content.Context
import com.cauri.core.data.model.PlaceSuggestion
import com.cauri.feature.places.R

/** « Près de Bastos · 6 arrêts ce mois », or the stops alone when the neighbourhood is unknown. */
internal fun suggestionText(context: Context, suggestion: PlaceSuggestion): String {
    val stops = context.resources.getQuantityString(
        R.plurals.places_suggestion_stops,
        suggestion.stops,
        suggestion.stops,
    )
    return suggestion.area?.let { context.getString(R.string.places_suggestion_near, it, stops) } ?: stops
}
