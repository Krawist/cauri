package com.cauri.core.capture.reminder

import java.time.Instant

/**
 * What the reminder engine learns: each night, the outcomes of the decisions, then the bandit trained again on the
 * last 90 days of the log, published as a new version once it has learnt enough; the rules decide until then.
 */
interface ReminderLearner {
    /**
     * Gives the decisions their outcome, then trains the bandit on the valid ones of the last 90 days, since the last
     * start from zero, and publishes it in one write. Running it again on the same log publishes nothing new.
     */
    suspend fun learn(now: Instant)

    /** The model that decides; `null` while the rules do, or when it learnt from another layout of the features. */
    suspend fun current(): PublishedModel?

    /** Forgets what was learnt: the models go, and the decisions before [now] no longer count. */
    suspend fun reset(now: Instant)
}
