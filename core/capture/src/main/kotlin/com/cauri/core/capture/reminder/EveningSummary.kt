package com.cauri.core.capture.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** The evening summary of the cards waiting, checked every quarter of an hour by a worker. */
object EveningSummary {
    /** Action of the intent the summary opens the app with: « Ce soir ». */
    const val ACTION_OPEN_SUMMARY = "com.cauri.action.OPEN_EVENING_SUMMARY"

    private const val WORK_NAME = "evening_summary"

    // The shortest period Android allows: the summary comes within a quarter of an hour of its time.
    private const val PERIOD_MINUTES = 15L

    /** Plans the check every quarter of an hour, once: calling it again keeps the planned one. */
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<EveningSummaryWorker>(PERIOD_MINUTES, TimeUnit.MINUTES).build(),
        )
    }
}
