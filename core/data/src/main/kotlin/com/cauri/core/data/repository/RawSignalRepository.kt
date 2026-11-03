package com.cauri.core.data.repository

import java.time.Instant

/** The notifications of the watched senders, kept as received: the source of truth of the capture. */
interface RawSignalRepository {
    /**
     * Keeps the notification of [sender] (package name) with its [title] and [text], posted at [receivedAt]. Returns
     * `false`, keeping nothing, when the same notification was already kept in the last minutes: apps post some
     * notifications again when they update them.
     */
    suspend fun record(sender: String, title: String?, text: String, receivedAt: Instant): Boolean
}
