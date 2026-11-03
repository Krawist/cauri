package com.cauri.core.capture.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Checks every quarter of an hour whether the evening summary is due ([EveningSummarySender]). */
@HiltWorker
internal class EveningSummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sender: EveningSummarySender,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        sender.sendIfDue()
        return Result.success()
    }
}
