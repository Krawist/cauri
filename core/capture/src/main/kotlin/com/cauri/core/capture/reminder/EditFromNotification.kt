package com.cauri.core.capture.reminder

/** « Autre » in the notification of a payment: the app opens on the edit sheet of its card. */
object EditFromNotification {
    /** Action of the intent « Autre » opens the app with. */
    const val ACTION_EDIT_TRANSACTION = "com.cauri.action.EDIT_TRANSACTION"

    /** Extra of that intent: the transaction of the card, a `Long`. */
    const val EXTRA_TRANSACTION = "transactionId"

    /** Extra of that intent: the notification to clear once the app opened, an `Int`. */
    const val EXTRA_NOTIFICATION = "notificationId"
}
