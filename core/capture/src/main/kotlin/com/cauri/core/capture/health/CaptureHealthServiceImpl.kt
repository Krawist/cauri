package com.cauri.core.capture.health

import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CaptureHealthService]. */
@Singleton
internal class CaptureHealthServiceImpl @Inject constructor() : CaptureHealthService {
    override fun silent(lastSignal: Instant?, now: Instant): Boolean =
        lastSignal != null && Duration.between(lastSignal, now) >= SILENCE

    override fun shouldWarn(lastSignal: Instant?, lastWarning: Instant?, blocked: Boolean, now: Instant): Boolean =
        blocked &&
            silent(lastSignal = lastSignal, now = now) &&
            (lastWarning == null || Duration.between(lastWarning, now) >= BETWEEN_WARNINGS)

    private companion object {
        val SILENCE: Duration = Duration.ofDays(30)
        val BETWEEN_WARNINGS: Duration = Duration.ofDays(7)
    }
}
