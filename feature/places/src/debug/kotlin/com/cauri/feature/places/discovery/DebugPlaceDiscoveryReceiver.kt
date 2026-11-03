package com.cauri.feature.places.discovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only: makes up a stop of half an hour at `lat` and `lon`, `days` days ago (0 by default) at `hour`
 * (10 by default), then looks for places to suggest, to check the question without stopping at a shop three days.
 */
@AndroidEntryPoint
internal class DebugPlaceDiscoveryReceiver : BroadcastReceiver() {
    @Inject
    lateinit var discoverer: PlaceDiscoverer

    override fun onReceive(context: Context, intent: Intent) {
        val latitude = intent.getStringExtra("lat")?.toDoubleOrNull() ?: return
        val longitude = intent.getStringExtra("lon")?.toDoubleOrNull() ?: return
        val days = intent.getStringExtra("days")?.toLongOrNull() ?: 0
        val hour = intent.getStringExtra("hour")?.toIntOrNull() ?: DEFAULT_HOUR
        val start = ZonedDateTime.now().minusDays(days).withHour(hour).withMinute(0).toInstant()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Three positions a quarter of an hour apart: a stop of half an hour.
                repeat(POSITIONS) {
                    discoverer.record(
                        latitude = latitude,
                        longitude = longitude,
                        at =
                        start + QUARTER.multipliedBy(it.toLong()),
                    )
                }
                discoverer.ask(Instant.now())
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val DEFAULT_HOUR = 10
        const val POSITIONS = 3
        val QUARTER: Duration = Duration.ofMinutes(15)
    }
}
