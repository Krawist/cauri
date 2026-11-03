package com.cauri.core.capture.signal

/** The senders whose notifications Cauri keeps. */
interface WatchedSenderRepository {
    /** The list shipped with the app. */
    val watched: WatchedSenders
}
