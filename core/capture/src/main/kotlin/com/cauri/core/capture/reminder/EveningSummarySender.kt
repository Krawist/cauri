package com.cauri.core.capture.reminder

/**
 * Sends the evening summary once a day, at its time ([com.cauri.core.engine.SummaryTime]) and before 23 h, only when
 * cards wait.
 */
internal interface EveningSummarySender {
    /** Sends the summary when it is due and something waits. */
    suspend fun sendIfDue()
}
