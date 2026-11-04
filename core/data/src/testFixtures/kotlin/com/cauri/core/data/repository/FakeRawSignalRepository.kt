package com.cauri.core.data.repository

import com.cauri.core.data.model.RawSignal
import java.time.Instant

/**
 * In-memory [RawSignalRepository] for tests: recorded signals get the ids 1, 2, 3… and stay unparsed; those marked read
 * by the model are kept.
 */
class FakeRawSignalRepository : RawSignalRepository {
    /** The signals [unparsed] gives, in order. */
    val signals = mutableListOf<RawSignal>()

    override suspend fun record(sender: String, title: String?, text: String, receivedAt: Instant): Boolean {
        signals += RawSignal(
            id = signals.size + 1L,
            sender = sender,
            title = title,
            text = text,
            receivedAt = receivedAt,
        )
        return true
    }

    override suspend fun unparsed(): List<RawSignal> = signals.toList()

    val readByModel = mutableListOf<Long>()

    override suspend fun unreadByModel(since: Instant, limit: Int): List<RawSignal> =
        signals.filter { it.id !in readByModel && !it.receivedAt.isBefore(since) }.take(limit)

    override suspend fun markReadByModel(id: Long) {
        readByModel += id
    }

    override suspend fun lastReceivedAt(): Instant? = signals.maxOfOrNull { it.receivedAt }
}
