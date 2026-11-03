package com.cauri.core.capture

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.cauri.core.capture.signal.NotificationJournal
import com.cauri.core.capture.signal.SignalFilterService
import com.cauri.core.capture.signal.SignalProcessor
import com.cauri.core.capture.signal.WatchedSenderRepository
import com.cauri.core.data.repository.RawSignalRepository
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps, as raw signals, the notifications of the watched senders (`watched_senders.json`): the apps of the operators
 * and the banks, and in the SMS and mail apps only their messages. Nothing else is kept. Each kept signal is then read
 * by the [SignalProcessor]; the notifications still shown when it connects are read too. Android binds it again after
 * a reboot while the access is given; it asks to be bound again when it gets disconnected.
 */
@AndroidEntryPoint
class CauriNotificationListener : NotificationListenerService() {
    @Inject
    internal lateinit var senders: WatchedSenderRepository

    @Inject
    internal lateinit var filter: SignalFilterService

    @Inject
    internal lateinit var rawSignals: RawSignalRepository

    @Inject
    internal lateinit var journal: NotificationJournal

    @Inject
    internal lateinit var processor: SignalProcessor

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(notification: StatusBarNotification) {
        val sender = notification.packageName
        val extras = notification.notification.extras
        val summary = notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        if (sender == packageName || summary) return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.let(filter::plain)
        val text = (
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            )
            ?.toString()
            ?.let(filter::plain)
            .orEmpty()
        val kept = text.isNotBlank() && filter.keeps(watched = senders.watched, packageName = sender, title = title)
        journal.seen(packageName = sender, title = title, kept = kept)
        if (kept) {
            scope.launch {
                val recorded = rawSignals.record(
                    sender = sender,
                    title = title,
                    text = text,
                    receivedAt = Instant.ofEpochMilli(notification.postTime),
                )
                if (recorded) processor.process()
            }
        }
    }

    // The notifications still shown were posted while the listener was not bound, or before a fix: each is kept once,
    // a notification read again having the same time as when it was posted.
    override fun onListenerConnected() {
        activeNotifications?.forEach(::onNotificationPosted)
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, CauriNotificationListener::class.java))
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
