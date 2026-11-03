package com.cauri.core.capture.reminder

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** What the reminder engine learns at night, while the phone charges and is not used. */
object ReminderLearning {
    private const val WORK_NAME = "reminder_learning"
    private const val PERIOD_HOURS = 24L

    /** Plans the nightly work once a day, charging and idle; calling it again keeps the planned one. */
    fun schedule(context: Context) {
        val constraints = Constraints.Builder().setRequiresCharging(true).setRequiresDeviceIdle(true).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ReminderLearningWorker>(PERIOD_HOURS, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build(),
        )
    }
}
