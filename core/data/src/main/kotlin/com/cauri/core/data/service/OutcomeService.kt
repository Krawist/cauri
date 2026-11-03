package com.cauri.core.data.service

import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.DecisionOutcome
import java.time.Duration
import java.time.Instant

/**
 * The outcomes the answers of the user do not give: a decision whose card waits too long, or whose visit was paid
 * during it, and the taps by mistake.
 */
internal interface OutcomeService {
    /**
     * The outcome of the open [decision] at [now]; `null` while it may still be answered. A visit whose card went, a
     * payment made during it: typed by hand, missed after a skip and confirmed otherwise ([paidByHand]); read in a
     * message, nothing spent in cash. A card waiting [TIMEOUT] after the decision: timed out, corrected by a late
     * answer.
     */
    fun outcomeOf(decision: DecisionLogEntity, paidByHand: Boolean, now: Instant): DecisionOutcome?

    /** Whether the closed [decision] was a tap by mistake: a notification set aside within [ACCIDENTAL]. */
    fun accidental(decision: DecisionLogEntity): Boolean

    companion object {
        /** How long a card waits for an answer before its decision times out. */
        val TIMEOUT: Duration = Duration.ofDays(3)

        /** Under it, a notification set aside was a tap by mistake. */
        val ACCIDENTAL: Duration = Duration.ofSeconds(2)
    }
}
