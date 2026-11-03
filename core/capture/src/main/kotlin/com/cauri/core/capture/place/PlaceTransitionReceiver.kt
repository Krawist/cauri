package com.cauri.core.capture.place

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the transitions of Cauri's geofences, when the user enters or leaves a place. Nothing is recorded yet: the
 * visits come with CAU-34.
 */
internal class PlaceTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
