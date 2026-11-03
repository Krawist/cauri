package com.cauri.core.capture.reminder

import com.cauri.core.data.model.PendingOperation

/** The notification of a card the reminder engine decided to remind at once. */
internal interface ReminderNotifier {
    /**
     * Shows [operation]: a visit asks what was spent, with its usual amount, « Rien » and « Autre montant » typed in
     * the notification; an expense read in a message asks what it was, with its likely categories. A tap opens
     * Accueil. `false` when Cauri may not show notifications.
     */
    suspend fun show(operation: PendingOperation): Boolean

    /** Takes the notification of the card [transactionId] off, once answered. */
    fun dismiss(transactionId: Long)
}
