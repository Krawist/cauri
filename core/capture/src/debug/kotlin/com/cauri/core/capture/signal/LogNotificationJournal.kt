package com.cauri.core.capture.signal

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/** [NotificationJournal] of debug builds: `adb logcat -s CauriCapture` lists the senders seen. */
@Singleton
internal class LogNotificationJournal @Inject constructor() : NotificationJournal {
    override fun seen(packageName: String, title: String?, kept: Boolean) {
        Log.d(TAG, "${if (kept) "kept" else "ignored"} · $packageName · ${title.orEmpty()}")
    }

    private companion object {
        const val TAG = "CauriCapture"
    }
}
