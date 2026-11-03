package com.cauri.core.capture.reminder

import com.cauri.core.data.model.NewBanditSnapshot
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.BanditRepository
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.engine.Bandit
import com.cauri.core.engine.FeatureEncoder
import com.cauri.core.engine.ReminderAction
import com.cauri.core.engine.ReminderOutcome
import com.cauri.core.engine.RewardTable
import com.cauri.core.engine.TrainingSample
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Default implementation for [ReminderLearner]. */
@Singleton
internal class ReminderLearnerImpl @Inject constructor(
    private val log: DecisionLogRepository,
    private val models: BanditRepository,
    private val preferences: UserPreferencesRepository,
    private val bandit: Bandit,
    private val rewards: RewardTable,
) : ReminderLearner {
    override suspend fun learn(now: Instant) {
        log.attributeOutcomes(now)
        val since = listOfNotNull(now - WINDOW, preferences.learningSince.first()).max()
        val decisions = log.trainingSet(since).filter { it.featureVersion == FeatureEncoder.VERSION }
        val latest = models.latest()
        // The same log gives the same model: nothing new to publish.
        val unchanged = latest != null &&
            latest.lastDecisionId == decisions.lastOrNull()?.id &&
            latest.decisions == decisions.size &&
            latest.rewardVersion == RewardTable.VERSION
        if (unchanged) return
        val samples = decisions.map {
            val action = ReminderAction.valueOf(it.action.name)
            TrainingSample(
                features = it.features,
                action = action,
                reward = rewards.reward(action, ReminderOutcome.valueOf(it.outcome.name)),
            )
        }
        val model = bandit.train(samples, size = FeatureEncoder.SIZE) ?: return
        models.publish(
            NewBanditSnapshot(
                trainedAt = now,
                featureVersion = model.featureVersion,
                rewardVersion = model.rewardVersion,
                lastDecisionId = decisions.last().id,
                decisions = decisions.size,
                parameters = bandit.encode(model),
            ),
        )
    }

    override suspend fun current(): PublishedModel? {
        val latest = models.latest()?.takeIf { it.featureVersion == FeatureEncoder.VERSION } ?: return null
        val model = bandit.decode(
            parameters = latest.parameters,
            size = FeatureEncoder.SIZE,
            featureVersion = latest.featureVersion,
            rewardVersion = latest.rewardVersion,
        )
        return PublishedModel(version = latest.version, model = model)
    }

    override suspend fun reset(now: Instant) {
        models.deleteAll()
        preferences.setLearningSince(now)
    }

    private companion object {
        // The window the bandit learns from: older habits fade.
        val WINDOW: Duration = Duration.ofDays(90)
    }
}
