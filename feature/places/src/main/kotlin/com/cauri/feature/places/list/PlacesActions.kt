package com.cauri.feature.places.list

/** What the user does on « Mes lieux ». */
internal interface PlacesActions {
    fun onBack()

    /** Opens the place [id], or a new one when `null`. */
    fun onOpenPlace(id: Long?)

    /** Opens the system screen where the location is allowed « all the time ». */
    fun onAllowLocation()

    /** « Oui, l'ajouter » of the suggestion [id]: the screen of a new place there. */
    fun onAddSuggestion(id: Long)

    /** « Non, ignorer » of the suggestion [id]: the place is ignored for good. */
    fun onIgnoreSuggestion(id: Long)
}
