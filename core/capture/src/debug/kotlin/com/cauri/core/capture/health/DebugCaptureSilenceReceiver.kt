package com.cauri.core.capture.health

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit

private const val PREFERENCES = "cauri_debug"
private const val SILENCE = "capture_silence"

/**
 * Debug builds only: simulates a month without message (`on` extra, true by default) or ends it (`false`), so that
 * Accueil shows its card the next time it opens.
 */
internal class DebugCaptureSilenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val on = intent.getBooleanExtra("on", true)
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit { putBoolean(SILENCE, on) }
    }
}

/** Whether a silence is simulated in this debug build. */
internal fun simulatedSilence(context: Context): Boolean =
    context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(SILENCE, false)
