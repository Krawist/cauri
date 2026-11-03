package com.cauri.core.capture.signal

import javax.inject.Inject
import javax.inject.Singleton

/** [NotificationJournal] of release builds: notes nothing, the senders stay private. */
@Singleton
internal class SilentNotificationJournal @Inject constructor() : NotificationJournal {
    override fun seen(packageName: String, title: String?, kept: Boolean) = Unit
}
