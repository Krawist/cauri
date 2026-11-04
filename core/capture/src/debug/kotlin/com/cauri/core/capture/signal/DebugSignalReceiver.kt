package com.cauri.core.capture.signal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cauri.core.data.repository.RawSignalRepository
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only: keeps the message of the `title` and `text` extras as if the app of the `app` extra had shown it
 * (Samsung Messages without it; Gmail for a mail), then reads it as the listener does, to check the templates and the
 * detected transactions without waiting for a real SMS or mail.
 */
@AndroidEntryPoint
internal class DebugSignalReceiver : BroadcastReceiver() {
    @Inject
    lateinit var rawSignals: RawSignalRepository

    @Inject
    lateinit var processor: SignalProcessor

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val text = intent.getStringExtra("text").orEmpty()
        val app = intent.getStringExtra("app") ?: SMS_APP
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recorded = rawSignals.record(
                    sender = app,
                    title = title,
                    text = text,
                    receivedAt = Instant.now(),
                )
                if (recorded) processor.process()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val SMS_APP = "com.samsung.android.messaging"
    }
}
