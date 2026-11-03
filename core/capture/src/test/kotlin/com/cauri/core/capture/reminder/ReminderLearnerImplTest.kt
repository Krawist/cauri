package com.cauri.core.capture.reminder

import com.cauri.core.data.model.ReminderAction
import com.cauri.core.data.model.ReminderOutcome
import com.cauri.core.data.model.TrainingDecision
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeBanditRepository
import com.cauri.core.data.repository.FakeDecisionLogRepository
import com.cauri.core.engine.BanditImpl
import com.cauri.core.engine.FeatureEncoder
import com.cauri.core.engine.ReminderAction as EngineAction
import com.cauri.core.engine.RewardTableImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ReminderLearnerImplTest {
    private val log = FakeDecisionLogRepository()
    private val models = FakeBanditRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val learner = ReminderLearnerImpl(
        log = log,
        models = models,
        preferences = preferences,
        bandit = BanditImpl(),
        rewards = RewardTableImpl(),
    )

    @Nested
    inner class Learn {
        @Test
        fun `should attribute the outcomes, then publish a model once enough decisions are valid`() = runTest {
            // given
            log.training = decisions(perAction = 60)

            // when
            learner.learn(NOW)

            // then
            log.attributions shouldContainExactly listOf(NOW)
            models.models.single().let { it.version to it.decisions } shouldBe (1L to 180)
        }

        @Test
        fun `should leave the rules deciding with too few decisions`() = runTest {
            // given
            log.training = decisions(perAction = 10)

            // when
            learner.learn(NOW)

            // then
            models.models.shouldBeEmpty()
        }

        @Test
        fun `should publish nothing new on the same log`() = runTest {
            // given
            log.training = decisions(perAction = 60)
            learner.learn(NOW)

            // when
            learner.learn(NOW.plusSeconds(3_600))

            // then
            models.models shouldHaveSize 1
        }
    }

    @Nested
    inner class Current {
        @Test
        fun `should give the latest model back as it learnt it`() = runTest {
            // given
            log.training = decisions(perAction = 60)
            learner.learn(NOW)

            // when
            val current = learner.current()

            // then
            current.shouldNotBeNull().let { it.version to it.model.arms.getValue(engineSkip()).decisions } shouldBe
                (1L to 60)
        }
    }

    @Nested
    inner class Reset {
        @Test
        fun `should forget the models and learn only from now`() = runTest {
            // given
            log.training = decisions(perAction = 60)
            learner.learn(NOW)

            // when
            learner.reset(NOW)

            // then
            learner.current().shouldBeNull()
            preferences.learningSince.value shouldBe NOW
        }
    }

    private fun engineSkip() = EngineAction.SKIP

    private fun decisions(perAction: Int): List<TrainingDecision> = ReminderAction.entries.flatMap { action ->
        List(perAction) { index ->
            TrainingDecision(
                id = action.ordinal * 1_000L + index,
                features = List(FeatureEncoder.SIZE) { if (it == index % FeatureEncoder.SIZE) 1.0 else 0.0 },
                featureVersion = FeatureEncoder.VERSION,
                action = action,
                outcome = ReminderOutcome.CONFIRMED,
            )
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-03T02:00:00Z")
    }
}
