package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.service.DiscoveryServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DiscoveryRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: DiscoveryRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = DiscoveryRepositoryImpl(database.discoveryDao(), database.placeDao(), DiscoveryServiceImpl())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class RecordPosition {
        @Test
        fun `should make a stop of two positions close to each other`() = runTest {
            // when
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON)
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON + QUARTER)

            // then
            database.discoveryDao().getStopsSince(Instant.EPOCH).map { it.positions } shouldContainExactly listOf(2)
        }

        @Test
        fun `should forget a position alone once no other joined it`() = runTest {
            // given
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON)
            repository.recordPosition(
                latitude = LATITUDE + 0.01,
                longitude = LONGITUDE,
                at = NOON + Duration.ofHours(1),
            )

            // when
            repository.findSuggestions(at = NOON + Duration.ofHours(1), zone = ZONE)

            // then
            database.discoveryDao().getEndedBetween(from = Instant.EPOCH, to = NOON + Duration.ofDays(1))
                .map { it.latitude } shouldContainExactly listOf(LATITUDE + 0.01)
        }

        @Test
        fun `should make a stop of positions read before a later one`() = runTest {
            // given
            repository.recordPosition(latitude = LATITUDE + 0.01, longitude = LONGITUDE, at = NOON + Duration.ofDays(2))

            // when
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON)
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON + QUARTER)

            // then
            database.discoveryDao().getStopsSince(Instant.EPOCH).map { it.positions } shouldContainExactly listOf(2)
        }

        @Test
        fun `should join each position to its own stop when two days come mixed`() = runTest {
            // when
            listOf(Duration.ZERO, QUARTER, QUARTER.multipliedBy(2)).forEach {
                repository.recordPosition(
                    latitude = LATITUDE,
                    longitude = LONGITUDE,
                    at =
                    NOON + Duration.ofDays(2) + it,
                )
                repository.recordPosition(latitude = LATITUDE + 0.01, longitude = LONGITUDE, at = NOON + it)
            }

            // then
            database.discoveryDao().getStopsSince(Instant.EPOCH).map { it.positions } shouldContainExactly listOf(3, 3)
        }

        @Test
        fun `should delete the stops older than 30 days`() = runTest {
            // given
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON)
            repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = NOON + QUARTER)

            // when
            repository.recordPosition(
                latitude = LATITUDE + 0.01,
                longitude = LONGITUDE,
                at = NOON + Duration.ofDays(31),
            )

            // then
            database.discoveryDao().getStopsSince(Instant.EPOCH).shouldBeEmpty()
        }
    }

    @Nested
    inner class FindSuggestions {
        @Test
        fun `should save the place stopped at on 3 days, then never find it again`() = runTest {
            // given
            listOf(0L, 2L, 5L).forEach { day ->
                val at = NOON + Duration.ofDays(day)
                repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = at)
                repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = at + QUARTER)
            }

            // when
            val found = repository.findSuggestions(at = NOON + Duration.ofDays(6), zone = ZONE)
            val again = repository.findSuggestions(at = NOON + Duration.ofDays(6), zone = ZONE)

            // then
            found.map { it.stops } shouldContainExactly listOf(3)
            again.shouldBeEmpty()
            repository.pending.first().map { it.id } shouldContainExactly listOf(found.single().id)
        }
    }

    @Nested
    inner class Answer {
        @Test
        fun `should leave no question pending once refused`() = runTest {
            // given
            listOf(0L, 2L, 5L).forEach { day ->
                val at = NOON + Duration.ofDays(day)
                repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = at)
                repository.recordPosition(latitude = LATITUDE, longitude = LONGITUDE, at = at + QUARTER)
            }
            val suggestion = repository.findSuggestions(at = NOON + Duration.ofDays(6), zone = ZONE).single()

            // when
            repository.answer(id = suggestion.id, accepted = false)

            // then
            repository.pending.first().shouldBeEmpty()
        }
    }

    private companion object {
        const val LATITUDE = 3.88
        const val LONGITUDE = 11.5
        val ZONE: ZoneId = ZoneId.of("Africa/Douala")
        val NOON: Instant = Instant.parse("2026-10-01T11:00:00Z")
        val QUARTER: Duration = Duration.ofMinutes(15)
    }
}
