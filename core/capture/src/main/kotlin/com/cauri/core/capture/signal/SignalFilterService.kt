package com.cauri.core.capture.signal

/** Which notifications Cauri keeps: those of the watched senders, nothing else. */
interface SignalFilterService {
    /**
     * Whether to keep the notification posted by [packageName] with [title]: any notification of a watched app, and
     * in an SMS or mail app only the messages of a watched sender, whatever the case and spaces of its name.
     */
    fun keeps(watched: WatchedSenders, packageName: String, title: String?): Boolean
}
