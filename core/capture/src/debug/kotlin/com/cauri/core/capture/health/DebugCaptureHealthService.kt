package com.cauri.core.capture.health

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug builds only: [CaptureHealthServiceImpl], unless a silence is simulated ([DebugCaptureSilenceReceiver]), when
 * the capture is always [silent] so that Accueil shows its card. The warning keeps its real rule.
 */
@Singleton
internal class DebugCaptureHealthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: CaptureHealthServiceImpl,
) : CaptureHealthService {
    override fun silent(lastSignal: Instant?, now: Instant): Boolean =
        simulatedSilence(context) || service.silent(lastSignal = lastSignal, now = now)

    override fun shouldWarn(lastSignal: Instant?, lastWarning: Instant?, blocked: Boolean, now: Instant): Boolean =
        service.shouldWarn(lastSignal = lastSignal, lastWarning = lastWarning, blocked = blocked, now = now)
}
