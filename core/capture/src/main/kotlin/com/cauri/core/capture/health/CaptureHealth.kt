package com.cauri.core.capture.health

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** The check of the capture, once a day by [CaptureHealthWorker]. */
object CaptureHealth {
    /** Action of the intent the warning opens the app with: the app opens Réglages. */
    const val ACTION_OPEN_SETTINGS = "com.cauri.action.OPEN_SETTINGS"

    private const val WORK_NAME = "capture_health"
    private const val CHECK_HOURS = 24L

    /** Plans the daily check, once: calling it again keeps the planned one. */
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CaptureHealthWorker>(CHECK_HOURS, TimeUnit.HOURS).build(),
        )
    }
}
