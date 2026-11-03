package com.cauri.core.capture.place

/** What Android tells about the user and a place. */
enum class PlaceTransition {
    /** The user has stayed in the place's area long enough for a visit, five minutes. */
    STAYED,

    /** The user left the place's area. */
    LEFT,
}
