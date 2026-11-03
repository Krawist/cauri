package com.cauri.core.capture.place

import android.annotation.SuppressLint
import android.content.Context
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionService
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Default implementation for [LocationService]. */
@Singleton
internal class LocationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: PermissionService,
) : LocationService {
    // The permission is checked just before.
    @SuppressLint("MissingPermission")
    override suspend fun currentPosition(): Position? {
        if (permissions.status().location == LocationAccess.DENIED) return null
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_AGE_MILLIS)
            .build()
        return withTimeoutOrNull(TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val cancellation = CancellationTokenSource()
                continuation.invokeOnCancellation { cancellation.cancel() }
                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(request, cancellation.token)
                    .addOnSuccessListener { location ->
                        continuation.resume(
                            location?.let {
                                Position(
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                    accuracyMeters = it.accuracy.roundToInt(),
                                )
                            },
                        )
                    }
                    .addOnFailureListener { continuation.resume(null) }
            }
        }
    }

    private companion object {
        // A position found within the last half minute is still where the user stands.
        const val MAX_AGE_MILLIS = 30_000L

        const val TIMEOUT_MILLIS = 20_000L
    }
}
