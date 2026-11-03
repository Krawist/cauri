package com.cauri.core.engine

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BanditImplTest {
    private val bandit = BanditImpl()

    @Nested
    inner class Train {
        @Test
        fun `should leave the rules deciding while too few decisions are valid`() {
            // given
            val samples = learnt(perAction = 40).dropLast(80)

            // when
            val model = bandit.train(samples, size = SIZE)

            // then
            model.shouldBeNull()
        }

        @Test
        fun `should wait for enough decisions of each action`() {
            // given
            val samples = learnt(perAction = 100).filterNot { it.action == ReminderAction.SKIP } +
                learnt(perAction = 5).filter { it.action == ReminderAction.SKIP }

            // when
            val model = bandit.train(samples, size = SIZE)

            // then
            model.shouldBeNull()
        }

        @Test
        fun `should learn each action from its own decisions`() {
            // when
            val model = bandit.train(learnt(perAction = 60), size = SIZE)

            // then
            model.shouldNotBeNull().arms.mapValues { it.value.decisions } shouldBe
                ReminderAction.entries.associateWith { 120 }
        }

        @Test
        fun `should learn the same model from the same log`() {
            // when
            val first = bandit.train(learnt(perAction = 60), size = SIZE)
            val second = bandit.train(learnt(perAction = 60), size = SIZE)

            // then
            first shouldBe second
        }
    }

    @Nested
    inner class Choose {
        @Test
        fun `should mostly notify where notifying was rewarded`() {
            // given
            val model = bandit.train(learnt(perAction = 60), size = SIZE).shouldNotBeNull()

            // when
            val choice = bandit.choose(model, visit(), ReminderAction.entries.toSet(), Random(1))

            // then
            choice.action shouldBe ReminderAction.NOTIFY_NOW
            choice.probability shouldBeGreaterThan 0.8
        }

        @Test
        fun `should mostly stay quiet where staying quiet was rewarded`() {
            // given
            val model = bandit.train(learnt(perAction = 60), size = SIZE).shouldNotBeNull()

            // when
            val choice = bandit.choose(model, payment(), ReminderAction.entries.toSet(), Random(1))

            // then
            choice.action shouldBe ReminderAction.SKIP
        }

        @Test
        fun `should choose only among the actions the guardrails allow`() {
            // given
            val model = bandit.train(learnt(perAction = 60), size = SIZE).shouldNotBeNull()

            // when
            val choice = bandit.choose(
                model,
                visit(),
                setOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP),
                Random(1),
            )

            // then
            (choice.action == ReminderAction.NOTIFY_NOW) shouldBe false
        }
    }

    @Nested
    inner class Decode {
        @Test
        fun `should give back the model it encoded`() {
            // given
            val model = bandit.train(learnt(perAction = 60), size = SIZE).shouldNotBeNull()

            // when
            val decoded = bandit.decode(
                bandit.encode(model),
                size = SIZE,
                featureVersion = model.featureVersion,
                rewardVersion = model.rewardVersion,
            )

            // then
            decoded shouldBe model
        }
    }

    // Visits answered when notified, payments best left alone: each action twice per context, [perAction] times.
    private fun learnt(perAction: Int): List<TrainingSample> = (0 until perAction).flatMap {
        listOf(
            TrainingSample(visit(), ReminderAction.NOTIFY_NOW, 1.0),
            TrainingSample(visit(), ReminderAction.EVENING_SUMMARY, 0.2),
            TrainingSample(visit(), ReminderAction.SKIP, -1.0),
            TrainingSample(payment(), ReminderAction.NOTIFY_NOW, -0.5),
            TrainingSample(payment(), ReminderAction.EVENING_SUMMARY, -0.2),
            TrainingSample(payment(), ReminderAction.SKIP, 0.5),
        )
    }.sortedBy { it.action }.groupBy { it.action }.flatMap { it.value }

    private fun visit() = listOf(1.0, 0.0, 1.0)

    private fun payment() = listOf(0.0, 1.0, 1.0)

    private companion object {
        const val SIZE = 3
    }
}
