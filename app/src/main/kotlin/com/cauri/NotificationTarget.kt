package com.cauri

import android.content.Intent
import com.cauri.core.capture.health.CaptureHealth
import com.cauri.core.capture.reminder.EditFromNotification
import com.cauri.core.capture.reminder.EveningSummary
import com.cauri.feature.cash.reminder.CountReminders
import com.cauri.feature.places.discovery.PlaceDiscovery

/** A screen a notification opens the app on. */
internal sealed interface NotificationTarget {
    /** A count reminder: the cash count. */
    data object CashCount : NotificationTarget

    /** The warning that the capture stopped: Réglages. */
    data object Settings : NotificationTarget

    /** « Oui » to a place often stopped at: the screen of a new place there. */
    data object SuggestedPlace : NotificationTarget

    /** The evening summary: « Ce soir ». */
    data object EveningSummaryScreen : NotificationTarget

    /**
     * « Autre » in the notification of a payment: the edit sheet of its card.
     *
     * @property transactionId The card's transaction.
     */
    data class EditTransaction(val transactionId: Long) : NotificationTarget

    companion object {
        /** The screen [intent] asks for; `null` for an ordinary launch. */
        fun of(intent: Intent?): NotificationTarget? = when (intent?.action) {
            CountReminders.ACTION_COUNT_CASH -> CashCount
            CaptureHealth.ACTION_OPEN_SETTINGS -> Settings
            PlaceDiscovery.ACTION_ADD_SUGGESTED_PLACE -> SuggestedPlace
            EveningSummary.ACTION_OPEN_SUMMARY -> EveningSummaryScreen
            EditFromNotification.ACTION_EDIT_TRANSACTION ->
                intent.getLongExtra(EditFromNotification.EXTRA_TRANSACTION, 0).takeIf { it > 0 }?.let(::EditTransaction)
            else -> null
        }
    }
}
