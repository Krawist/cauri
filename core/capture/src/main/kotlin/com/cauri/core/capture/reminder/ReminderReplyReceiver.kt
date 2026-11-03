package com.cauri.core.capture.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.cauri.core.data.repository.ConfirmationRepository
import com.cauri.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The buttons of a reminder notification, answered without opening the app: an amount or « Autre montant » typed
 * confirms the card of a visit, a category confirms an expense read, « Rien » sets the visit aside.
 */
@AndroidEntryPoint
internal class ReminderReplyReceiver : BroadcastReceiver() {
    /** What a button answers. */
    enum class Reply {
        /** The usual amount of the visit. */
        AMOUNT,

        /** An amount typed in the notification. */
        TYPED,

        /** « Rien »: nothing spent. */
        NOTHING,

        /** A category for an expense read. */
        CATEGORY,
    }

    @Inject
    lateinit var transactions: TransactionRepository

    @Inject
    lateinit var confirmations: ConfirmationRepository

    @Inject
    lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getLongExtra(EXTRA_TRANSACTION, -1)
        val reply = Reply.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_REPLY) }
        val typed = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(EXTRA_TYPED)
            ?.filter(Char::isDigit)?.toString()?.toLongOrNull()?.takeIf { it in 1..MAX_AMOUNT }
        // A typed amount must be one.
        val answered = reply != null && (reply != Reply.TYPED || typed != null)
        if (transactionId <= 0 || !answered) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactions.pending.first().firstOrNull { it.transactionId == transactionId }?.let { operation ->
                    when (reply) {
                        Reply.AMOUNT, Reply.TYPED -> confirmations.confirm(
                            operation = operation,
                            categoryId = operation.categoryId,
                            proposedCategoryId = operation.categoryId,
                            amount = typed ?: intent.getLongExtra(EXTRA_AMOUNT, 0),
                        )
                        Reply.CATEGORY -> confirmations.confirm(
                            operation = operation,
                            categoryId = intent.getLongExtra(EXTRA_CATEGORY, -1).takeIf { it > 0 },
                            proposedCategoryId = intent.getLongExtra(EXTRA_PROPOSED_CATEGORY, -1).takeIf { it > 0 },
                            amount = operation.amount,
                        )
                        Reply.NOTHING -> confirmations.dismiss(transactionId = transactionId, type = operation.type)
                    }
                }
                notifier.dismiss(transactionId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "com.cauri.action.REMINDER_REPLY"
        const val EXTRA_TRANSACTION = "transaction"
        const val EXTRA_REPLY = "reply"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_TYPED = "typed"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_PROPOSED_CATEGORY = "proposedCategory"

        // Up to 10 000 000 F, as the quick entry.
        private const val MAX_AMOUNT = 10_000_000L
    }
}
