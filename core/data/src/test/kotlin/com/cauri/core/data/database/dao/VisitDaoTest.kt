package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.PlaceKind
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VisitDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var visitDao: VisitDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        visitDao = database.visitDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetOngoing {
        @Test
        fun `should return the visits the user has not left`() = runTest {
            // given
            val placeId = database.placeDao().insert(
                PlaceEntity(
                    name = "Marché Mokolo",
                    latitude = 3.88,
                    longitude = 11.50,
                    radiusMeters = 80f,
                    kind = PlaceKind.SHOP,
                    defaultCategoryId = null,
                ),
            )
            val ongoing = VisitEntity(placeId = placeId, enteredAt = Instant.ofEpochMilli(2_000), leftAt = null)
            val ongoingId = visitDao.insert(ongoing)
            visitDao.insert(
                VisitEntity(placeId = placeId, enteredAt = Instant.EPOCH, leftAt = Instant.ofEpochMilli(1_000)),
            )

            // when
            val visits = visitDao.getOngoing()

            // then
            visits shouldContainExactly listOf(ongoing.copy(id = ongoingId))
        }
    }

    @Nested
    inner class IsAtHomeOrWork {
        @Test
        fun `should tell the user is at home while a visit there is open`() = runTest {
            // given
            val homeId = database.placeDao().insert(market().copy(name = "Maison", kind = PlaceKind.HOME))
            visitDao.enter(placeId = homeId, enteredAt = Instant.EPOCH)

            // when
            val atHome = visitDao.isAtHomeOrWork()

            // then
            atHome.shouldBeTrue()
        }

        @Test
        fun `should not count a shop`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            visitDao.enter(placeId = placeId, enteredAt = Instant.EPOCH)

            // when
            val atHome = visitDao.isAtHomeOrWork()

            // then
            atHome.shouldBeFalse()
        }

        @Test
        fun `should not count a visit to work already left`() = runTest {
            // given
            val workId = database.placeDao().insert(market().copy(name = "Bureau", kind = PlaceKind.WORK))
            visitDao.enter(placeId = workId, enteredAt = Instant.EPOCH)
            visitDao.leave(placeId = workId, leftAt = Instant.ofEpochSecond(600))

            // when
            val atWork = visitDao.isAtHomeOrWork()

            // then
            atWork.shouldBeFalse()
        }
    }

    @Nested
    inner class Enter {
        @Test
        fun `should open a visit to the place`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())

            // when
            visitDao.enter(placeId = placeId, enteredAt = Instant.EPOCH)

            // then
            visitDao.getOngoing().map { it.placeId to it.enteredAt } shouldContainExactly
                listOf(placeId to Instant.EPOCH)
        }

        @Test
        fun `should keep the stay already open there`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            visitDao.enter(placeId = placeId, enteredAt = Instant.EPOCH)

            // when
            visitDao.enter(placeId = placeId, enteredAt = Instant.ofEpochSecond(600))

            // then
            visitDao.getOngoing().map { it.enteredAt } shouldContainExactly listOf(Instant.EPOCH)
        }
    }

    @Nested
    inner class Leave {
        @Test
        fun `should close the visit not left yet`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            visitDao.enter(placeId = placeId, enteredAt = Instant.EPOCH)

            // when
            visitDao.leave(placeId = placeId, leftAt = Instant.ofEpochSecond(1_200))

            // then
            visitDao.getOngoing().shouldBeEmpty()
        }

        @Test
        fun `should open a new visit after the user left`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            visitDao.enter(placeId = placeId, enteredAt = Instant.EPOCH)
            visitDao.leave(placeId = placeId, leftAt = Instant.ofEpochSecond(1_200))

            // when
            visitDao.enter(placeId = placeId, enteredAt = Instant.ofEpochSecond(3_600))

            // then
            visitDao.getOngoing().map { it.enteredAt } shouldContainExactly listOf(Instant.ofEpochSecond(3_600))
        }
    }

    private fun market() = PlaceEntity(
        name = "Marché Mokolo",
        latitude = 3.88,
        longitude = 11.50,
        radiusMeters = 80f,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
    )
}
