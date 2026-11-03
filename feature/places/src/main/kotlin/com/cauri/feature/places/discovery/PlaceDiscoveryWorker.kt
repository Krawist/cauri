package com.cauri.feature.places.discovery

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Reads the position of the phone every quarter of an hour and asks about the places often stopped at. */
@HiltWorker
internal class PlaceDiscoveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val discoverer: PlaceDiscoverer,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        discoverer.discover()
        return Result.success()
    }
}
