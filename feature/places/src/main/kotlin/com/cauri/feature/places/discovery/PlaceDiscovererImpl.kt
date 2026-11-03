package com.cauri.feature.places.discovery

import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.place.AddressService
import com.cauri.core.capture.place.LocationService
import com.cauri.core.data.repository.DiscoveryRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [PlaceDiscoverer]. */
// Injected: the position, its address, the permission, the stops and the notification.
@Suppress("LongParameterList")
@Singleton
internal class PlaceDiscovererImpl @Inject constructor(
    private val permissions: PermissionService,
    private val location: LocationService,
    private val addresses: AddressService,
    private val discovery: DiscoveryRepository,
    private val notifier: PlaceSuggestionNotifier,
    private val clock: Clock,
) : PlaceDiscoverer {
    override suspend fun discover() {
        if (permissions.status().location != LocationAccess.ALWAYS) return
        val position = location.currentPosition(precise = false) ?: return
        val now = clock.instant()
        record(latitude = position.latitude, longitude = position.longitude, at = now)
        ask(now)
    }

    override suspend fun record(latitude: Double, longitude: Double, at: Instant) =
        discovery.recordPosition(latitude = latitude, longitude = longitude, at = at)

    override suspend fun ask(at: Instant) {
        // One question at a time: the others wait in « Mes lieux », one after the other.
        val found = discovery.findSuggestions(at = at, zone = clock.zone).firstOrNull() ?: return
        val area = addresses.addressAt(latitude = found.latitude, longitude = found.longitude)?.area
        discovery.setArea(id = found.id, area = area)
        if (notifier.show(found.copy(area = area))) discovery.markNotified(id = found.id, at = at)
    }
}
