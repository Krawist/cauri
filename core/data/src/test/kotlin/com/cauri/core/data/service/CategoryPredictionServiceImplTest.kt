package com.cauri.core.data.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CategoryPredictionServiceImplTest {
    private val predictions = CategoryPredictionServiceImpl()

    @Nested
    inner class Predict {
        @Test
        fun `should put first the category chosen most often`() {
            // given
            val uses =
                listOf(use(category = 1, daysAgo = 1), use(category = 2, daysAgo = 2), use(category = 2, daysAgo = 3))

            // when
            val predicted = predictions.predict(uses, NOW)

            // then
            predicted shouldContainExactly listOf(2L, 1L)
        }

        @Test
        fun `should let a recent correction win over an old habit`() {
            // given
            val uses = List(3) { use(category = 1, daysAgo = 120L + it) } + listOf(use(category = 2, daysAgo = 1))

            // when
            val predicted = predictions.predict(uses, NOW)

            // then
            predicted shouldContainExactly listOf(2L, 1L)
        }

        @Test
        fun `should predict nothing without history`() {
            // when & then
            predictions.predict(emptyList(), NOW).shouldBeEmpty()
        }
    }

    private fun use(category: Long, daysAgo: Long) = CategoryUse(
        categoryId = category,
        occurredAt =
        NOW - Duration.ofDays(daysAgo),
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
