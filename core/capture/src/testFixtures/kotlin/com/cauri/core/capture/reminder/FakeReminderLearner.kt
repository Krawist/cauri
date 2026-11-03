package com.cauri.core.capture.reminder

import java.time.Instant

/** [ReminderLearner] for tests: it gives the model the test sets, and keeps when it learnt and started again. */
class FakeReminderLearner : ReminderLearner {
    var published: PublishedModel? = null

    val learnt = mutableListOf<Instant>()

    val resets = mutableListOf<Instant>()

    override suspend fun learn(now: Instant) {
        learnt += now
    }

    override suspend fun current() = published

    override suspend fun reset(now: Instant) {
        resets += now
        published = null
    }
}
