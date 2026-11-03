package com.cauri.core.data.service

import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.StopEntity
import com.cauri.core.data.database.entity.SuggestionAnswer
import com.cauri.core.data.model.PlaceKind
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DiscoveryServiceImplTest {
    private val service = DiscoveryServiceImpl()

    @Nested
    inner class Continues {
        @Test
        fun `should keep the phone at the stop a quarter of an hour later, a few metres away`() {
            // given
            val stop = stop(day = 0, hour = 10)

            // when
            val continues = service.continues(
                stop,
                latitude = MARKET_LATITUDE + 0.0005,
                longitude = MARKET_LONGITUDE,
                at =
                stop.endedAt + QUARTER,
            )

            // then
            continues.shouldBeTrue()
        }

        @Test
        fun `should start another stop farther than 150 m`() {
            // given
            val stop = stop(day = 0, hour = 10)

            // when
            val continues = service.continues(
                stop,
                latitude = MARKET_LATITUDE + 0.002,
                longitude = MARKET_LONGITUDE,
                at =
                stop.endedAt + QUARTER,
            )

            // then
            continues.shouldBeFalse()
        }

        @Test
        fun `should start another stop after a gap of an hour`() {
            // given
            val stop = stop(day = 0, hour = 10)

            // when
            val continues = service.continues(
                stop,
                latitude = MARKET_LATITUDE,
                longitude = MARKET_LONGITUDE,
                at =
                stop.endedAt + Duration.ofHours(1),
            )

            // then
            continues.shouldBeFalse()
        }
    }

    @Nested
    inner class Suggestions {
        @Test
        fun `should suggest a place stopped at on 3 different days`() {
            // when
            val found =
                suggest(stops = listOf(stop(day = 1, hour = 10), stop(day = 3, hour = 17), stop(day = 8, hour = 9)))

            // then
            found.map { it.stops } shouldBe listOf(3)
        }

        @Test
        fun `should not suggest a place stopped at three times the same day`() {
            // when
            val found =
                suggest(stops = listOf(stop(day = 1, hour = 9), stop(day = 1, hour = 12), stop(day = 1, hour = 17)))

            // then
            found.shouldBeEmpty()
        }

        @Test
        fun `should not suggest where the user sleeps`() {
            // when
            val found =
                suggest(stops = listOf(stop(day = 1, hour = 10), stop(day = 3, hour = 23), stop(day = 8, hour = 9)))

            // then
            found.shouldBeEmpty()
        }

        @Test
        fun `should not suggest a place declared already`() {
            // when
            val found = suggest(
                stops = listOf(stop(day = 1, hour = 10), stop(day = 3, hour = 17), stop(day = 8, hour = 9)),
                places = listOf(market()),
            )

            // then
            found.shouldBeEmpty()
        }

        @Test
        fun `should not ask again about a place refused`() {
            // when
            val found = suggest(
                stops = listOf(stop(day = 1, hour = 10), stop(day = 3, hour = 17), stop(day = 8, hour = 9)),
                known = listOf(refused()),
            )

            // then
            found.shouldBeEmpty()
        }

        @Test
        fun `should leave out the stops older than 30 days`() {
            // when
            val found =
                suggest(stops = listOf(stop(day = -40, hour = 10), stop(day = 3, hour = 17), stop(day = 8, hour = 9)))

            // then
            found.shouldBeEmpty()
        }

        @Test
        fun `should suggest two places apart`() {
            // given
            val shop = listOf(1, 3, 8).map { stop(day = it, hour = 10, latitude = MARKET_LATITUDE + 0.01) }

            // when
            val found = suggest(
                stops =
                listOf(stop(day = 1, hour = 15), stop(day = 3, hour = 15), stop(day = 8, hour = 15)) + shop,
            )

            // then
            found shouldHaveSize 2
        }
    }

    private fun suggest(
        stops: List<StopEntity>,
        places: List<PlaceEntity> = emptyList(),
        known: List<PlaceSuggestionEntity> = emptyList(),
    ) = service.suggestions(stops = stops, places = places, known = known, zone = ZONE, at = NOW)

    // A stop of half an hour, [day] days after the start of the month, from [hour] in Douala.
    private fun stop(day: Int, hour: Int, latitude: Double = MARKET_LATITUDE): StopEntity {
        val start = MONTH_START.plusDays(day.toLong()).withHour(hour).toInstant()
        return StopEntity(
            latitude = latitude,
            longitude = MARKET_LONGITUDE,
            startedAt = start,
            endedAt = start + Duration.ofMinutes(30),
            positions = 3,
        )
    }

    private fun market() = PlaceEntity(
        name = "Marché Mokolo",
        latitude = MARKET_LATITUDE,
        longitude = MARKET_LONGITUDE,
        radiusMeters = 80f,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
    )

    private fun refused() = PlaceSuggestionEntity(
        latitude = MARKET_LATITUDE,
        longitude = MARKET_LONGITUDE,
        area = null,
        stops = 3,
        foundAt = NOW,
        notifiedAt = NOW,
        answer = SuggestionAnswer.REFUSED,
    )

    private companion object {
        const val MARKET_LATITUDE = 3.88
        const val MARKET_LONGITUDE = 11.5
        val ZONE: ZoneId = ZoneId.of("Africa/Douala")
        val MONTH_START = Instant.parse("2026-10-01T00:00:00Z").atZone(ZONE)
        val NOW: Instant = Instant.parse("2026-10-20T12:00:00Z")
        val QUARTER: Duration = Duration.ofMinutes(15)
    }
}
