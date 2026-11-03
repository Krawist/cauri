package com.cauri.feature.places.discovery

import com.cauri.core.data.model.PlaceSuggestion

/** Asks « Tu viens souvent ici. C'est un commerce ? » with a notification. */
internal interface PlaceSuggestionNotifier {
    /** Shows the question about [suggestion], « Oui » and « Non »; `false` when Cauri may not show notifications. */
    fun show(suggestion: PlaceSuggestion): Boolean

    /** Takes the question off the notifications, once answered. */
    fun dismiss()
}
