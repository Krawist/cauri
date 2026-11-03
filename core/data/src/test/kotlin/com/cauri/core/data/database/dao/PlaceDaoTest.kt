package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.PlaceKind
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PlaceDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var placeDao: PlaceDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        placeDao = database.placeDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `should list the places by name`() = runTest {
            // given
            val market =
                PlaceEntity(
                    name = "Marché Mokolo",
                    latitude = 3.88,
                    longitude = 11.50,
                    radiusMeters = 80f,
                    kind = PlaceKind.SHOP,
                    defaultCategoryId = null,
                )
            val home =
                PlaceEntity(
                    name = "Maison",
                    latitude = 3.86,
                    longitude = 11.52,
                    radiusMeters = 50f,
                    kind = PlaceKind.HOME,
                    defaultCategoryId = null,
                )
            val marketId = placeDao.insert(market)
            val homeId = placeDao.insert(home)

            // when
            val places = placeDao.observeAll().first()

            // then
            places shouldContainExactly listOf(home.copy(id = homeId), market.copy(id = marketId))
        }
    }
}
