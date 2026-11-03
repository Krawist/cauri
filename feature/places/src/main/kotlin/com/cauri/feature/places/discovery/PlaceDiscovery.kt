package com.cauri.feature.places.discovery

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** The discovery of the places the user often stops at, a position read every quarter of an hour by a worker. */
object PlaceDiscovery {
    /** Action of the intent « Oui » opens the app with: the screen of a new place, at the place suggested. */
    const val ACTION_ADD_SUGGESTED_PLACE = "com.cauri.action.ADD_SUGGESTED_PLACE"

    private const val WORK_NAME = "place_discovery"

    // The shortest period Android allows.
    private const val PERIOD_MINUTES = 15L

    /** Plans the position read every quarter of an hour, once: calling it again keeps the planned one. */
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PlaceDiscoveryWorker>(PERIOD_MINUTES, TimeUnit.MINUTES).build(),
        )
    }
}
