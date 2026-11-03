package com.cauri.core.capture.signal

import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [SignalFilterService]. */
@Singleton
internal class SignalFilterServiceImpl @Inject constructor() : SignalFilterService {
    override fun keeps(watched: WatchedSenders, packageName: String, title: String?): Boolean {
        val sender = title?.let(::plain)?.trim()
        val fromWatchedApp = watched.apps.any { it.packageName == packageName }
        val fromWatchedSender = sender != null &&
            watched.messageApps.any { it.packageName == packageName } &&
            watched.messageSenders.any { it.name.trim().equals(sender, ignoreCase = true) }
        return fromWatchedApp || fromWatchedSender
    }

    override fun plain(text: String): String = text.filterNot { it in DIRECTION_MARKS }

    private companion object {
        // Left-to-right and right-to-left marks, embeddings, overrides and isolates: they change no letter.
        val DIRECTION_MARKS = setOf('\u200E', '\u200F') + ('\u202A'..'\u202E') + ('\u2066'..'\u2069')
    }
}
