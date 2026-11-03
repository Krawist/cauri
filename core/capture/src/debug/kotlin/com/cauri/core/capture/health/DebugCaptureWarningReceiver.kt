package com.cauri.core.capture.health

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Debug builds only: shows at once the warning that Cauri receives no more messages, without a month of silence. */
@AndroidEntryPoint
internal class DebugCaptureWarningReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notifier: CaptureHealthNotifier

    override fun onReceive(context: Context, intent: Intent) {
        notifier.show()
    }
}
