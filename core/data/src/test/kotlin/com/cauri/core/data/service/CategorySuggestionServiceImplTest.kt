package com.cauri.core.data.service

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CategorySuggestionServiceImplTest {
    private val service = CategorySuggestionServiceImpl()

    @Nested
    inner class Suggest {
        @Test
        fun `should put first the category most used around this time of day`() {
            // given
            val uses = listOf(
                CategoryUse(categoryId = 2, occurredAt = Instant.parse("2026-10-20T07:45:00Z")),
                CategoryUse(categoryId = 3, occurredAt = Instant.parse("2026-10-21T19:00:00Z")),
                CategoryUse(categoryId = 3, occurredAt = Instant.parse("2026-10-22T19:00:00Z")),
            )

            // when
            val suggested = service.suggest(
                uses = uses,
                categories = categories(),
                at = Instant.parse("2026-11-03T08:30:00Z"),
                zone = ZoneOffset.UTC,
                limit = 3,
            )

            // then
            suggested.map { it.id } shouldContainExactly listOf(2L, 3L, 1L)
        }

        @Test
        fun `should count a use across midnight within the slot`() {
            // given
            val uses = listOf(CategoryUse(categoryId = 3, occurredAt = Instant.parse("2026-10-20T23:30:00Z")))

            // when
            val suggested = service.suggest(
                uses = uses,
                categories = categories(),
                at = Instant.parse("2026-11-03T00:30:00Z"),
                zone = ZoneOffset.UTC,
                limit = 1,
            )

            // then
            suggested.map { it.id } shouldContainExactly listOf(3L)
        }

        @Test
        fun `should keep the given order without any use`() {
            // when
            val suggested = service.suggest(
                uses = emptyList(),
                categories = categories(),
                at = Instant.parse("2026-11-03T08:30:00Z"),
                zone = ZoneOffset.UTC,
                limit = 2,
            )

            // then
            suggested.map { it.id } shouldContainExactly listOf(1L, 2L)
        }
    }

    private fun categories() = listOf(
        Category(id = 1, name = "Alimentation", nature = CategoryNature.EXPENSE),
        Category(id = 2, name = "Snacks", nature = CategoryNature.EXPENSE),
        Category(id = 3, name = "Transport", nature = CategoryNature.EXPENSE),
    )
}
