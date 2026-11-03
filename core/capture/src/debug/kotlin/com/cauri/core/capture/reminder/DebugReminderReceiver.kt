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

/**
 * Debug builds only: shows the reminder notification of the card `transaction` (its id), or of the newest card when
 * none is given, whatever the rules would decide, to check it at any hour.
 */
@AndroidEntryPoint
internal class DebugReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var transactions: TransactionRepository

    @Inject
    lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val wanted = intent.getStringExtra("transaction")?.toLongOrNull()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cards = transactions.pending.first()
                (cards.firstOrNull { it.transactionId == wanted } ?: cards.firstOrNull())?.let { notifier.show(it) }
            } finally {
                pending.finish()
            }
        }
    }
}
