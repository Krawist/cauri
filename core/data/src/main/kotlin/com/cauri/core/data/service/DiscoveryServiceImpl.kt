package com.cauri.core.data.service

import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.StopEntity
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Default implementation for [DiscoveryService]. */
@Singleton
internal class DiscoveryServiceImpl @Inject constructor() : DiscoveryService {
    override fun continues(stop: StopEntity, latitude: Double, longitude: Double, at: Instant): Boolean =
        !at.isBefore(stop.endedAt) &&
            Duration.between(stop.endedAt, at) <= DiscoveryService.GAP &&
            distance(stop.latitude, stop.longitude, latitude, longitude) <= STOP_RADIUS_METERS

    override fun suggestions(
        stops: List<StopEntity>,
        places: List<PlaceEntity>,
        known: List<PlaceSuggestionEntity>,
        zone: ZoneId,
        at: Instant,
    ): List<PlaceSuggestionEntity> {
        val since = at - DiscoveryService.WINDOW
        return clusters(stops.filter { it.endedAt >= since })
            .filter { cluster -> cluster.stops.none { it.atNight(zone) } }
            .filter { cluster ->
                cluster.stops.map { it.startedAt.atZone(zone).toLocalDate() }.distinct().size >=
                    MIN_DAYS
            }
            .filter { cluster ->
                places.none {
                    distance(it.latitude, it.longitude, cluster.latitude, cluster.longitude) <=
                        max(it.radiusMeters.toDouble(), STOP_RADIUS_METERS)
                } &&
                    known.none {
                        distance(it.latitude, it.longitude, cluster.latitude, cluster.longitude) <=
                            STOP_RADIUS_METERS
                    }
            }
            .map {
                PlaceSuggestionEntity(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    area = null,
                    stops = it.stops.size,
                    foundAt = at,
                    notifiedAt = null,
                    answer = null,
                )
            }
    }

    // Stops within 150 m of the centre of a group join it, in the order they happened; the centre follows them.
    private fun clusters(stops: List<StopEntity>): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        stops.sortedBy { it.startedAt }.forEach { stop ->
            val index = clusters.indexOfFirst {
                distance(it.latitude, it.longitude, stop.latitude, stop.longitude) <= STOP_RADIUS_METERS
            }
            if (index < 0) {
                clusters += Cluster(latitude = stop.latitude, longitude = stop.longitude, stops = listOf(stop))
            } else {
                val cluster = clusters[index]
                val count = cluster.stops.size + 1
                clusters[index] = Cluster(
                    latitude = cluster.latitude + (stop.latitude - cluster.latitude) / count,
                    longitude = cluster.longitude + (stop.longitude - cluster.longitude) / count,
                    stops = cluster.stops + stop,
                )
            }
        }
        return clusters
    }

    // Whether the stop touches the night, from 22 h to 5 h.
    private fun StopEntity.atNight(zone: ZoneId): Boolean {
        val start = startedAt.atZone(zone)
        val end = endedAt.atZone(zone)
        fun night(hour: Int) = hour >= NIGHT_START || hour < NIGHT_END
        return night(start.hour) || night(end.hour) || start.toLocalDate() != end.toLocalDate()
    }

    // Distance in metres between two positions, on a round Earth.
    private fun distance(fromLatitude: Double, fromLongitude: Double, toLatitude: Double, toLongitude: Double): Double {
        val latitudes = Math.toRadians(toLatitude - fromLatitude)
        val longitudes = Math.toRadians(toLongitude - fromLongitude)
        val a = sin(latitudes / 2).pow(2) +
            cos(Math.toRadians(fromLatitude)) * cos(Math.toRadians(toLatitude)) * sin(longitudes / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }

    /**
     * Stops close to each other.
     *
     * @property latitude Latitude of their centre, in degrees.
     * @property longitude Longitude of their centre, in degrees.
     * @property stops The stops, in the order they happened.
     */
    private data class Cluster(val latitude: Double, val longitude: Double, val stops: List<StopEntity>)

    private companion object {
        const val STOP_RADIUS_METERS = 150.0
        const val EARTH_RADIUS_METERS = 6_371_000.0
        const val MIN_DAYS = 3
        const val NIGHT_START = 22
        const val NIGHT_END = 5
    }
}
