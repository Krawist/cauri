package com.cauri.core.capture.signal

import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [SignalFilterService]. */
@Singleton
internal class SignalFilterServiceImpl @Inject constructor() : SignalFilterService {
    override fun keeps(watched: WatchedSenders, packageName: String, title: String?): Boolean {
        val sender = title?.trim()
        val fromWatchedApp = watched.apps.any { it.packageName == packageName }
        val fromWatchedSender = sender != null &&
            watched.messageApps.any { it.packageName == packageName } &&
            watched.messageSenders.any { it.name.trim().equals(sender, ignoreCase = true) }
        return fromWatchedApp || fromWatchedSender
    }
}
