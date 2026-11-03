package com.cauri.core.capture.signal

import kotlinx.serialization.Serializable

/**
 * The senders whose notifications Cauri keeps, from `watched_senders.json`: data, filled from real phones.
 *
 * @property apps Apps of the operators and the banks: all their notifications are kept.
 * @property messageApps SMS and mail apps: only the messages of [messageSenders] are kept, never the user's own.
 * @property messageSenders Operators and banks as they sign their SMS and mails.
 */
@Serializable
data class WatchedSenders(
    val apps: List<WatchedApp>,
    val messageApps: List<WatchedApp>,
    val messageSenders: List<WatchedMessageSender>,
)
