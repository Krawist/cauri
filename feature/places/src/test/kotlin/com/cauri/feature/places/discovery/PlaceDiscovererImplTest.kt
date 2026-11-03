package com.cauri.feature.places.discovery

import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.place.FakeAddressService
import com.cauri.core.capture.place.FakeLocationService
import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.capture.place.Position
import com.cauri.core.data.model.PlaceSuggestion
import com.cauri.core.data.repository.FakeDiscoveryRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PlaceDiscovererImplTest {
    private val permissions = FakePermissionService()
    private val location = FakeLocationService()
    private val addresses = FakeAddressService()
    private val discovery = FakeDiscoveryRepository()
    private val notifier = RecordingNotifier()
    private val discoverer = PlaceDiscovererImpl(
        permissions,
        location,
        addresses,
        discovery,
        notifier,
        Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneId.of("Africa/Douala")),
    )

    @Nested
    inner class Discover {
        @Test
        fun `should keep the position of the phone`() = runTest {
            // given
            permissions.status = permissions.status.copy(location = LocationAccess.ALWAYS)
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 400)

            // when
            discoverer.discover()

            // then
            discovery.positions shouldContainExactly listOf(3.88 to 11.5)
        }

        @Test
        fun `should read nothing without the location all the time`() = runTest {
            // given
            permissions.status = permissions.status.copy(location = LocationAccess.WHILE_IN_USE)
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 400)

            // when
            discoverer.discover()

            // then
            discovery.positions.shouldBeEmpty()
        }
    }

    @Nested
    inner class Ask {
        @Test
        fun `should ask about the place found, near its neighbourhood, once`() = runTest {
            // given
            discovery.found += suggestion()
            addresses.address =
                FoundAddress(
                    label = "Bastos, Yaoundé",
                    name = null,
                    area = "Bastos",
                    latitude = 3.89,
                    longitude = 11.51,
                )

            // when
            discoverer.ask(Instant.EPOCH)

            // then
            notifier.shown.map { it.area } shouldContainExactly listOf("Bastos")
            discovery.notified shouldContainExactly listOf(5L)
        }

        @Test
        fun `should ask nothing when no place is found`() = runTest {
            // when
            discoverer.ask(Instant.EPOCH)

            // then
            notifier.shown.shouldBeEmpty()
        }
    }

    private fun suggestion() =
        PlaceSuggestion(id = 5, latitude = 3.89, longitude = 11.51, area = null, stops = 3, notified = false)

    private class RecordingNotifier : PlaceSuggestionNotifier {
        val shown = mutableListOf<PlaceSuggestion>()

        override fun show(suggestion: PlaceSuggestion): Boolean {
            shown += suggestion
            return true
        }

        override fun dismiss() = Unit
    }
}
