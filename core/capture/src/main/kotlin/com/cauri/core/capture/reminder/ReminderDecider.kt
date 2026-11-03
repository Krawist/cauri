package com.cauri.core.capture.reminder

/**
 * Asks the reminder engine about each new card to confirm, once: an operation read in a message, the card of a visit
 * ended or a withdrawal. Each decision is logged with its context, and « maintenant » sends the notification.
 */
internal interface ReminderDecider {
    /** Decides about the cards of the last two hours not decided yet. */
    suspend fun decidePending()
}
