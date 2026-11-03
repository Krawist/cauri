package com.cauri.feature.cash.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Cash count reminders, checked every hour by [CountReminderWorker]; the user's hour needs no new schedule. */
object CountReminders {
    /** Action of the intent a reminder opens the app with: the app opens the cash count. */
    const val ACTION_COUNT_CASH = "com.cauri.action.COUNT_CASH"

    private const val WORK_NAME = "count_reminders"
    private const val CHECK_HOURS = 1L

    /** Plans the hourly check, once: calling it again keeps the planned one. */
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CountReminderWorker>(CHECK_HOURS, TimeUnit.HOURS).build(),
        )
    }
}
