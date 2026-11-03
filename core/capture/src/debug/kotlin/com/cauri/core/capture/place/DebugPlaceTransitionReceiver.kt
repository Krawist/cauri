package com.cauri.core.capture.place

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only: tells that the user stayed at the place of the `place` extra (its id), or left it with
 * the boolean `left`, as its geofence would, to check the visits without walking to a shop.
 */
@AndroidEntryPoint
internal class DebugPlaceTransitionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var recorder: VisitRecorder

    override fun onReceive(context: Context, intent: Intent) {
        val placeId = intent.getStringExtra("place")?.toLongOrNull() ?: return
        val transition = if (intent.getBooleanExtra("left", false)) PlaceTransition.LEFT else PlaceTransition.STAYED
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                recorder.record(transition = transition, placeIds = listOf(placeId), at = Instant.now())
            } finally {
                pending.finish()
            }
        }
    }
}
