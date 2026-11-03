package com.cauri.core.capture.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cauri.core.data.repository.DecisionLogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock

/** The nightly work of the reminder engine: the outcomes of the decisions the user did not answer. */
@HiltWorker
internal class ReminderLearningWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val log: DecisionLogRepository,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        log.attributeOutcomes(clock.instant())
        return Result.success()
    }
}
