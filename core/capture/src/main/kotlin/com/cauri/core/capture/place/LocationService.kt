package com.cauri.core.capture.place

/** The position of the phone, to save a place where the user stands. */
interface LocationService {
    /**
     * Locates the phone now, by GPS when [precise], else by Wi-Fi and cell towers, which spares the battery; `null`
     * when the user did not allow the location, it is off, or Android found no position in time.
     */
    suspend fun currentPosition(precise: Boolean = true): Position?
}
