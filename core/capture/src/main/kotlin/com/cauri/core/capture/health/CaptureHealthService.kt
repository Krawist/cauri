package com.cauri.core.capture.health

import java.time.Instant

/**
 * Tells when the capture of the notifications seems to have stopped: a month without any message of the watched
 * senders. A user may go a month without a digital payment, but the banks write at least about their monthly fees.
 */
interface CaptureHealthService {
    /** Whether no message arrived for a month since [lastSignal], at [now]; never before the first message. */
    fun silent(lastSignal: Instant?, now: Instant): Boolean

    /**
     * Whether to warn the user at [now]: the capture is [silent], something may stop it ([blocked]: the access to the
     * notifications taken back, or the battery restricted) and the last warning, at [lastWarning], is a week old or
     * more. With every permission given, a silence is the user's own rhythm.
     */
    fun shouldWarn(lastSignal: Instant?, lastWarning: Instant?, blocked: Boolean, now: Instant): Boolean
}
