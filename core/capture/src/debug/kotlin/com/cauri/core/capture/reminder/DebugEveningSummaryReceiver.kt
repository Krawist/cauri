package com.cauri.core.capture.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cauri.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Debug builds only: shows the evening summary of the cards waiting at any hour, even when none waits. */
@AndroidEntryPoint
internal class DebugEveningSummaryReceiver : BroadcastReceiver() {
    @Inject
    lateinit var transactions: TransactionRepository

    @Inject
    lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notifier.showSummary(transactions.pending.first().size)
            } finally {
                pending.finish()
            }
        }
    }
}
