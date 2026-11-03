package com.cauri.feature.places.edit

import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.data.model.PlaceKind

/** What the user does on the screen of a place. */
// One function per gesture of the screen.
@Suppress("TooManyFunctions")
internal interface PlaceActions {
    fun onClose()

    /** Locates the phone: the place is where the user stands. */
    fun onLocate()

    /** Looks for the addresses matching [query], once the user stops typing. */
    fun onQuery(query: String)

    /** Puts the place at [address], found by the search. */
    fun onChooseAddress(address: FoundAddress)

    fun onName(name: String)

    /** Shop, home or work. */
    fun onKind(kind: PlaceKind)

    /** Chooses the default category [id], or none when it was already chosen. */
    fun onCategory(id: Long)

    /** Keeps [meters] as the radius. */
    fun onRadius(meters: Int)

    fun onRemindAfterVisit(remind: Boolean)

    /** Saves the place: « Enregistrer le lieu ». */
    fun onSave()

    /** Opens or closes the dialog asking to delete the place. */
    fun onAskDelete(open: Boolean)

    fun onDelete()
}
