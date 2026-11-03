package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.PlaceKind
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
}
