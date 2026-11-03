package com.cauri.core.data.repository

import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import java.time.Instant

/** The log of the reminder engine's decisions, kept to train the bandit, and what it reads of the past. */
interface DecisionLogRepository {
    /** Whether the engine already decided about the card [transactionId]: it decides once per card. */
    suspend fun hasDecision(transactionId: Long): Boolean

    /** The reminders and transactions so far, the reminders of the day counted from [dayStart]. */
    suspend fun stats(dayStart: Instant): ReminderStats

    /** The visit whose card is [transactionId] and the history of its place; `null` when the card is no visit's. */
    suspend fun visitHistory(transactionId: Long): VisitHistory?

    /** When the user answered a card (confirmed or « Rien dépensé ») since [since], the oldest first. */
    suspend fun answerTimes(since: Instant): List<Instant>

    /**
     * Gives an outcome to the decisions the user did not answer: a visit paid during it, a card waiting too long; and
     * leaves out of the training the notifications set aside by mistake. Running it again changes nothing.
     */
    suspend fun attributeOutcomes(now: Instant)

    /** Logs [decision], its outcome open until the user answers. */
    suspend fun log(decision: NewDecision)
}
