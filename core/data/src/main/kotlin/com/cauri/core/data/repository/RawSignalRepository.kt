package com.cauri.core.data.repository

import com.cauri.core.data.model.RawSignal
import java.time.Instant

/** The notifications of the watched senders, kept as received: the source of truth of the capture. */
interface RawSignalRepository {
    /**
     * Keeps the notification of [sender] (package name) with its [title] and [text], posted at [receivedAt]. Returns
     * `false`, keeping nothing, when the same notification was already kept in the last minutes: apps post some
     * notifications again when they update them.
     */
    suspend fun record(sender: String, title: String?, text: String, receivedAt: Instant): Boolean

    /** The signals no template read yet, oldest first: to read when they arrive, and again when templates change. */
    suspend fun unparsed(): List<RawSignal>

    /** When the last signal was received; `null` while Cauri kept none. */
    suspend fun lastReceivedAt(): Instant?
}
