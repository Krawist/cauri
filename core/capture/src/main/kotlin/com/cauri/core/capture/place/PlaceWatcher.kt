package com.cauri.core.capture.place

/** Keeps Android's geofences in line with the places the user declared. */
interface PlaceWatcher {
    /** Registers the geofences of the places watched, and again after each change of them; call it once per process. */
    fun start()

    /** Registers them again, such as when the user may have just allowed the location in the background. */
    fun refresh()
}
