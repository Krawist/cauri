package com.cauri.core.capture.signal

/** Which notifications Cauri keeps: those of the watched senders, nothing else. */
interface SignalFilterService {
    /**
     * Whether to keep the notification posted by [packageName] with [title]: any notification of a watched app, and
     * in an SMS or mail app only the messages of a watched sender, whatever the case and spaces of its name.
     */
    fun keeps(watched: WatchedSenders, packageName: String, title: String?): Boolean

    /**
     * [text] without the invisible direction marks some apps put around names: Samsung Messages titles a message
     * « \u2068OrangeMoney\u2069 ». The text is otherwise kept as received.
     */
    fun plain(text: String): String
}
