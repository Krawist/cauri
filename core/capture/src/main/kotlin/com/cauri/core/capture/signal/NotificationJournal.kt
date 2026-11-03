package com.cauri.core.capture.signal

/**
 * Notes the notifications the listener sees, to find the real senders on test phones. Debug builds log the package
 * name and title, never the text; release builds note nothing.
 */
interface NotificationJournal {
    /** [packageName] posted a notification titled [title]; [kept] whether Cauri kept it. */
    fun seen(packageName: String, title: String?, kept: Boolean)
}
