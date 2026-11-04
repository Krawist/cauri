package com.cauri.core.capture.place

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Default implementation for [AddressService]. */
@Singleton
internal class AddressServiceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    AddressService {
    private val geocoder by lazy { Geocoder(context, Locale.FRANCE) }

    override suspend fun search(query: String): List<FoundAddress> {
        if (!Geocoder.isPresent() || query.isBlank()) return emptyList()
        return geocode { done ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(query, MAX_RESULTS, SOUTH, WEST, NORTH, EAST, listener(done))
            } else {
                @Suppress("DEPRECATION") // The listener exists from Android 13 only.
                done(geocoder.getFromLocationName(query, MAX_RESULTS, SOUTH, WEST, NORTH, EAST).orEmpty())
            }
        }.mapNotNull { it.toFound() }
    }

    override suspend fun addressAt(latitude: Double, longitude: Double): FoundAddress? {
        if (!Geocoder.isPresent()) return null
        return geocode { done ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1, listener(done))
            } else {
                @Suppress("DEPRECATION") // The listener exists from Android 13 only.
                done(geocoder.getFromLocation(latitude, longitude, 1).orEmpty())
            }
        }.firstNotNullOfOrNull { it.toFound() }
    }

    // Runs a request of the geocoder off the main thread, which gives what it found to its callback; empty when it
    // fails, is offline or takes too long. The callback is a function of ours: Android's listener does not exist
    // before Android 13, and naming it there would end the app.
    private suspend fun geocode(request: ((List<Address>) -> Unit) -> Unit): List<Address> =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(TIMEOUT_MILLIS) {
                suspendCancellableCoroutine { continuation ->
                    val done = { addresses: List<Address> ->
                        if (continuation.isActive) continuation.resume(addresses)
                    }
                    try {
                        request(done)
                    } catch (_: IOException) {
                        done(emptyList())
                    } catch (_: IllegalArgumentException) {
                        done(emptyList())
                    }
                }
            }.orEmpty()
        }

    // Android's listener of a request, from Android 13, handing what it found, or nothing, to [done].
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun listener(done: (List<Address>) -> Unit) = object : Geocoder.GeocodeListener {
        override fun onGeocode(addresses: MutableList<Address>) = done(addresses.toList())

        override fun onError(errorMessage: String?) = done(emptyList())
    }

    // The place or street, then the neighbourhood and the town; `null` without a position or a name.
    private fun Address.toFound(): FoundAddress? {
        val name = placeName()
        val label = listOfNotNull(name ?: thoroughfare?.titled(), subLocality?.titled(), locality?.titled())
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
            .ifBlank { getAddressLine(0).orEmpty() }
        val area = (subLocality ?: locality)?.takeIf { it.isNotBlank() }?.titled()
        return FoundAddress(label = label, name = name, area = area, latitude = latitude, longitude = longitude)
            .takeIf { hasLatitude() && hasLongitude() && label.isNotBlank() }
    }

    // The name the map knows for the place: no number, so neither a street (« Rue 7.962 ») nor a plus code, and not
    // only its street or area.
    private fun Address.placeName(): String? = featureName
        ?.takeIf { name -> name.isNotBlank() && name.none { it.isDigit() } && '+' !in name }
        ?.takeIf { it != thoroughfare && it != subLocality && it != locality }
        ?.titled()

    // The geocoder may write a name in lower case (« bastos »).
    private fun String.titled() = replaceFirstChar { it.titlecase(Locale.FRANCE) }

    private companion object {
        const val MAX_RESULTS = 5
        const val TIMEOUT_MILLIS = 10_000L

        // Cameroon, so that « Bastos » is the neighbourhood of Yaoundé first.
        const val SOUTH = 1.6
        const val WEST = 8.4
        const val NORTH = 13.1
        const val EAST = 16.2
    }
}
