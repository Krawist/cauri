package com.cauri.core.capture.place

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Android forgets the geofences when the phone restarts. Receiving the restart starts Cauri's process, whose
 * application asks the [PlaceWatcher] to register them again: nothing else to do here.
 */
internal class PlaceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
