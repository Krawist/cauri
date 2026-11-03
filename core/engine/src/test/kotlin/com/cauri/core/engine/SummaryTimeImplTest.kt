package com.cauri.core.engine

import io.kotest.matchers.shouldBe
import java.time.LocalTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SummaryTimeImplTest {
    private val summaryTime = SummaryTimeImpl()

    @Nested
    inner class At {
        @Test
        fun `should keep the time the user chose`() {
            // when
            val at = summaryTime.at(chosen = LocalTime.of(19, 0), answers = List(12) { LocalTime.of(21, 10) })

            // then
            at shouldBe LocalTime.of(19, 0)
        }

        @Test
        fun `should come at 20 30 before enough answers`() {
            // when
            val at = summaryTime.at(chosen = null, answers = List(9) { LocalTime.of(19, 10) })

            // then
            at shouldBe LocalTime.of(20, 30)
        }

        @Test
        fun `should follow when the user usually answers in the evening, to the half hour`() {
            // given
            val answers = List(5) { LocalTime.of(19, 5) } + List(6) { LocalTime.of(19, 40) }

            // when
            val at = summaryTime.at(chosen = null, answers = answers)

            // then
            at shouldBe LocalTime.of(19, 30)
        }

        @Test
        fun `should leave out the answers given by day`() {
            // given
            val answers = List(20) { LocalTime.of(12, 0) } + List(10) { LocalTime.of(21, 0) }

            // when
            val at = summaryTime.at(chosen = null, answers = answers)

            // then
            at shouldBe LocalTime.of(21, 0)
        }

        @Test
        fun `should never come after 22 h`() {
            // when
            val at = summaryTime.at(chosen = null, answers = List(10) { LocalTime.of(22, 45) })

            // then
            at shouldBe LocalTime.of(22, 0)
        }
    }
}
