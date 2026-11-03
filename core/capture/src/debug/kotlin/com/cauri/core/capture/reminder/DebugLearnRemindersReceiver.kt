package com.cauri.core.capture.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only: runs the nightly work of the reminders at once, whether the phone charges or not: the outcomes,
 * then the bandit trained again.
 */
@AndroidEntryPoint
internal class DebugLearnRemindersReceiver : BroadcastReceiver() {
    @Inject
    lateinit var learner: ReminderLearner

    @Inject
    lateinit var clock: Clock

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                learner.learn(clock.instant())
            } finally {
                pending.finish()
            }
        }
    }
}
