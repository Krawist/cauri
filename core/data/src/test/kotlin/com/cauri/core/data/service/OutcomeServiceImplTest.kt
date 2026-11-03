package com.cauri.core.data.service

import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.model.DecisionExclusion
import com.cauri.core.data.model.ReminderAction
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OutcomeServiceImplTest {
    private val outcomes = OutcomeServiceImpl()

    @Nested
    inner class OutcomeOf {
        @Test
        fun `should wait for an answer within three days`() {
            // when
            val outcome = outcomes.outcomeOf(decision(), paidByHand = false, now = DECIDED + Duration.ofDays(2))

            // then
            outcome shouldBe null
        }

        @Test
        fun `should time out a card waiting three days`() {
            // when
            val outcome = outcomes.outcomeOf(decision(), paidByHand = false, now = DECIDED + Duration.ofDays(3))

            // then
            outcome shouldBe DecisionOutcome.TIMEOUT
        }

        @ParameterizedTest
        @CsvSource("SKIP, true, MISSED", "NOTIFY_NOW, true, CONFIRMED", "SKIP, false, NOTHING_SPENT")
        fun `should judge a visit whose card went by how it was paid`(
            action: ReminderAction,
            paidByHand: Boolean,
            expected: DecisionOutcome,
        ) {
            // given
            val decision = decision(action = action).copy(transactionId = null, visitId = 3)

            // when
            val outcome = outcomes.outcomeOf(decision, paidByHand = paidByHand, now = DECIDED)

            // then
            outcome shouldBe expected
        }

        @Test
        fun `should leave a decision already closed`() {
            // given
            val decision = decision().copy(outcome = DecisionOutcome.CONFIRMED, outcomeAt = DECIDED)

            // when
            val outcome = outcomes.outcomeOf(decision, paidByHand = false, now = DECIDED + Duration.ofDays(5))

            // then
            outcome shouldBe null
        }
    }

    @Nested
    inner class Accidental {
        @Test
        fun `should take a notification set aside within two seconds for a tap by mistake`() {
            // given
            val decision = decision().copy(outcome = DecisionOutcome.DISMISSED, outcomeAt = DECIDED.plusSeconds(1))

            // when & then
            outcomes.accidental(decision).shouldBeTrue()
        }

        @Test
        fun `should keep a notification set aside later`() {
            // given
            val decision = decision().copy(outcome = DecisionOutcome.DISMISSED, outcomeAt = DECIDED.plusSeconds(30))

            // when & then
            outcomes.accidental(decision).shouldBeFalse()
        }

        @Test
        fun `should keep a quick confirmation`() {
            // given
            val decision = decision().copy(outcome = DecisionOutcome.CONFIRMED, outcomeAt = DECIDED.plusSeconds(1))

            // when & then
            outcomes.accidental(decision).shouldBeFalse()
        }

        @Test
        fun `should not exclude a decision twice`() {
            // given
            val decision = decision().copy(
                outcome = DecisionOutcome.DISMISSED,
                outcomeAt = DECIDED.plusSeconds(1),
                excluded = DecisionExclusion.UNSEEN,
            )

            // when & then
            outcomes.accidental(decision).shouldBeFalse()
        }
    }

    private fun decision(action: ReminderAction = ReminderAction.NOTIFY_NOW) = DecisionLogEntity(
        decidedAt = DECIDED,
        visitId = null,
        transactionId = 7,
        features = listOf(1.0),
        featureVersion = 1,
        action = action,
        probability = 1.0,
        modelVersion = "rules-2",
        outcome = null,
        outcomeAt = null,
    )

    private companion object {
        val DECIDED: Instant = Instant.parse("2026-11-03T11:00:00Z")
    }
}
