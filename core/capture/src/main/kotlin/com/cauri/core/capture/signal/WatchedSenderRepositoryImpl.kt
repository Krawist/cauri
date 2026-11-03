package com.cauri.core.capture.signal

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Default implementation for [WatchedSenderRepository]: reads `watched_senders.json` once. */
@Singleton
internal class WatchedSenderRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    WatchedSenderRepository {
    override val watched: WatchedSenders by lazy {
        val json = context.assets.open(FILE).bufferedReader().use { it.readText() }
        Json.decodeFromString(WatchedSenders.serializer(), json)
    }

    private companion object {
        const val FILE = "watched_senders.json"
    }
}
