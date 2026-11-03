package com.cauri.core.capture.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock

/** The nightly work of the reminder engine: the outcomes of the decisions, then the bandit trained again. */
@HiltWorker
internal class ReminderLearningWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val learner: ReminderLearner,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        learner.learn(clock.instant())
        return Result.success()
    }
}
