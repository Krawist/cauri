package com.cauri.core.capture.signal

import kotlinx.serialization.Serializable

/** How sure Cauri is that a watched app posts the notifications it needs. */
@Serializable
enum class WatchedStatus {
    /** Seen posting them on a real phone. */
    CONFIRMED,

    /** Found on a real phone, but no notification of a transaction seen yet. */
    TO_CONFIRM,
}
