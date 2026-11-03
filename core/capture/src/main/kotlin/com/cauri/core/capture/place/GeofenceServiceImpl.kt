package com.cauri.core.capture.place

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.data.model.Place
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Default implementation for [GeofenceService]. */
@Singleton
internal class GeofenceServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: PermissionService,
) : GeofenceService {
    private val client by lazy { LocationServices.getGeofencingClient(context) }

    // Android sends every transition of Cauri's geofences to the receiver.
    private val transitions by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, PlaceTransitionReceiver::class.java),
            // Android fills in the transition: the intent must stay mutable.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    // The background location is checked just before.
    @SuppressLint("MissingPermission")
    override suspend fun watch(places: List<Place>): Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (available != ConnectionResult.SUCCESS) return false
        client.removeGeofences(transitions).done()
        val allowed = permissions.status().location == LocationAccess.ALWAYS
        return allowed && places.isNotEmpty() && client.addGeofences(request(places), transitions).done()
    }

    private fun request(places: List<Place>) = GeofencingRequest.Builder()
        .addGeofences(places.map { it.toGeofence() })
        .build()

    private fun Place.toGeofence() = Geofence.Builder()
        .setRequestId(id.toString())
        .setCircularRegion(latitude, longitude, radiusMeters.toFloat())
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .build()

    // Whether the task succeeded, once it ends.
    private suspend fun Task<Void>.done(): Boolean = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { continuation.resume(it.isSuccessful) }
    }
}
