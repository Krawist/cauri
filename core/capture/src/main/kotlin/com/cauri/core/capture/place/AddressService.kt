package com.cauri.core.capture.place

/**
 * Addresses from Android's geocoder, to place a place without standing there. By decision, it is
 * the one use of the network besides the catalogue and the backup: what the user types and the positions are sent to
 * the geocoding service of the phone, Google's on most of them.
 */
interface AddressService {
    /** The addresses matching [query] in Cameroon first, the best first; empty when none or offline. */
    suspend fun search(query: String): List<FoundAddress>

    /** The address at [latitude] and [longitude]; `null` when unknown or offline. */
    suspend fun addressAt(latitude: Double, longitude: Double): FoundAddress?
}
