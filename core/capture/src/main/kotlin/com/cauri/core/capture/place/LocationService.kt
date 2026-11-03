package com.cauri.core.capture.place

/** The position of the phone, to save a place where the user stands. */
interface LocationService {
    /**
     * Locates the phone now; `null` when the user did not allow the location, it is off, or Android found no position
     * in time.
     */
    suspend fun currentPosition(): Position?
}
