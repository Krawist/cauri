package com.cauri.core.capture.health

import java.time.Instant

/** [CaptureHealthService] for tests: it answers what it is told. */
class FakeCaptureHealthService : CaptureHealthService {
    /** What [silent] answers. */
    var silent = false

    /** What [shouldWarn] answers. */
    var warn = false

    override fun silent(lastSignal: Instant?, now: Instant) = silent

    override fun shouldWarn(lastSignal: Instant?, lastWarning: Instant?, blocked: Boolean, now: Instant) = warn
}
