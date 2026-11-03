package com.cauri.core.engine

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FeatureEncoderImplTest {
    private val encoder = FeatureEncoderImpl()

    @Nested
    inner class Encode {
        @Test
        fun `should give as many numbers as the layout says`() {
            // when
            val features = encoder.encode(visit(), rulesScore = 1.0)

            // then
            features shouldHaveSize FeatureEncoder.SIZE
        }

        @Test
        fun `should put midnight and 23 h close on the hour circle`() {
            // when
            val midnight = encoder.encode(visit(hour = 0), rulesScore = 1.0)
            val late = encoder.encode(visit(hour = 23), rulesScore = 1.0)

            // then
            late[3] shouldBe (midnight[3] plusOrMinus 0.05)
        }

        @Test
        fun `should put a stay of 25 minutes in its band`() {
            // when
            val features = encoder.encode(visit(stay = 25), rulesScore = 1.0)

            // then
            features[5] shouldBe 0.5
        }

        @Test
        fun `should count an unknown place halfway`() {
            // when
            val features = encoder.encode(visit(), rulesScore = 1.0)

            // then
            features[6] shouldBe 0.5
        }

        @Test
        fun `should keep the score of the rules last`() {
            // when
            val features = encoder.encode(visit(), rulesScore = 0.5)

            // then
            features.last() shouldBe 0.5
        }
    }

    private fun visit(hour: Int = 11, stay: Long = 10) = ReminderContext(
        trigger = ReminderTrigger.VISIT_END,
        at = ZonedDateTime.of(2026, 11, 3, hour, 0, 0, 0, ZoneId.of("Africa/Douala")),
        expense = true,
        stayMinutes = stay,
        placeVisits = 0,
        placeSpendRate = null,
        minutesSinceTransaction = 90,
        minutesSinceNotification = null,
        notificationsToday = 1,
        cashBalance = 5_000,
    )
}
