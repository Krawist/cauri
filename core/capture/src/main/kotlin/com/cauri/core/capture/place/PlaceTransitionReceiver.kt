package com.cauri.core.capture.place

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the transitions of Cauri's geofences, a stay of five minutes in a place or its departure, and has them
 * recorded as visits. Android starts Cauri for it, even closed.
 */
@AndroidEntryPoint
internal class PlaceTransitionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var recorder: VisitRecorder

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)?.takeUnless { it.hasError() } ?: return
        val transition = when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_DWELL -> PlaceTransition.STAYED
            Geofence.GEOFENCE_TRANSITION_EXIT -> PlaceTransition.LEFT
            else -> return
        }
        // Each geofence is named after the id of its place.
        val placeIds = event.triggeringGeofences.orEmpty().mapNotNull { it.requestId.toLongOrNull() }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                recorder.record(transition = transition, placeIds = placeIds, at = Instant.now())
            } finally {
                pending.finish()
            }
        }
    }
}
