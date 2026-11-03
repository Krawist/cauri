package com.cauri.core.data.service

import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PlaceServiceImplTest {
    private val service = PlaceServiceImpl()

    @Nested
    inner class Watched {
        @Test
        fun `should watch every place under the limit, the most visited first`() {
            // given
            val places = listOf(place(id = 1, visits = 2), place(id = 2, visits = 9), place(id = 3, visits = 0))

            // when
            val watched = service.watched(places)

            // then
            watched.map { it.id } shouldContainExactly listOf(2L, 1L, 3L)
        }

        @Test
        fun `should keep the 100 most visited places`() {
            // given
            val places = (1L..101L).map { place(id = it, visits = if (it == 1L) 0 else 5) }

            // when
            val watched = service.watched(places)

            // then
            watched shouldHaveSize 100
            watched.none { it.id == 1L } shouldBe true
        }

        @Test
        fun `should prefer the newest place between places as visited`() {
            // given
            val places = (1L..101L).map { place(id = it, visits = 0) }

            // when
            val watched = service.watched(places)

            // then
            watched.first().id shouldBe 101L
            watched.none { it.id == 1L } shouldBe true
        }
    }

    private fun place(id: Long, visits: Int) = Place(
        id = id,
        name = "Lieu $id",
        latitude = 3.86,
        longitude = 11.52,
        radiusMeters = 80,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
        remindAfterVisit = true,
        area = null,
        address = null,
        visits = visits,
    )
}
