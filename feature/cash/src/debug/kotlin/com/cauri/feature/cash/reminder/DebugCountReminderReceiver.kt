package com.cauri.feature.cash.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cauri.core.data.model.CountReminderReason
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Debug builds only: shows the count reminder of the `reason` extra (a [CountReminderReason] name, EVENING by default)
 * at once, through the same [CountReminderNotifier] as the evening check.
 */
@AndroidEntryPoint
internal class DebugCountReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notifier: CountReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val reason = intent.getStringExtra("reason")?.let(CountReminderReason::valueOf) ?: CountReminderReason.EVENING
        notifier.show(reason)
    }
}
