package com.cauri.core.engine

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/** Default implementation for [Bandit]. Public so that the app's Hilt modules can bind it. */
@Singleton
class BanditImpl @Inject constructor() : Bandit {
    override fun train(samples: List<TrainingSample>, size: Int): BanditModel? {
        val byAction = samples.groupBy { it.action }
        val enough = samples.size >= Bandit.MIN_DECISIONS &&
            ReminderAction.entries.all { (byAction[it]?.size ?: 0) >= Bandit.MIN_PER_ACTION }
        if (!enough) return null
        val arms = ReminderAction.entries.associateWith { action ->
            val precision = Matrices.identity(size, PRIOR).toMutableList()
            val rewards = MutableList(size) { 0.0 }
            val decisions = byAction.getValue(action)
            decisions.forEach { sample ->
                for (row in 0 until size) {
                    rewards[row] += sample.reward * sample.features[row]
                    for (column in 0 until size) {
                        precision[row * size + column] += sample.features[row] * sample.features[column]
                    }
                }
            }
            BanditArm(precision = precision, rewards = rewards, decisions = decisions.size)
        }
        return BanditModel(arms = arms, featureVersion = FeatureEncoder.VERSION, rewardVersion = RewardTable.VERSION)
    }

    override fun choose(
        model: BanditModel,
        features: List<Double>,
        allowed: Set<ReminderAction>,
        random: Random,
    ): BanditChoice {
        val size = features.size
        // For each action allowed, the mean of its weights and the root of their covariance.
        val posteriors = allowed.associateWith { action ->
            val arm = model.arms.getValue(action)
            val covariance = Matrices.inverse(arm.precision, size)
            Matrices.times(covariance, arm.rewards, size) to
                Matrices.cholesky(covariance.map { it * EXPLORATION * EXPLORATION }, size)
        }
        // The first draw decides; the others tell how often it would.
        val draws = List(DRAWS) {
            posteriors.maxBy { (_, posterior) ->
                val (mean, root) = posterior
                val noise = List(size) { gaussian(random) }
                Matrices.dot(mean, features) + Matrices.dot(Matrices.times(root, noise, size), features)
            }.key
        }
        val action = draws.first()
        return BanditChoice(action = action, probability = draws.count { it == action }.toDouble() / DRAWS)
    }

    override fun encode(model: BanditModel): List<Double> = ReminderAction.entries.flatMap { action ->
        val arm = model.arms.getValue(action)
        listOf(arm.decisions.toDouble()) + arm.precision + arm.rewards
    }

    override fun decode(parameters: List<Double>, size: Int, featureVersion: Int, rewardVersion: String): BanditModel {
        val armSize = 1 + size * size + size
        val arms = ReminderAction.entries.mapIndexed { index, action ->
            val arm = parameters.subList(index * armSize, (index + 1) * armSize)
            action to BanditArm(
                precision = arm.subList(1, 1 + size * size),
                rewards = arm.subList(1 + size * size, armSize),
                decisions = arm.first().toInt(),
            )
        }.toMap()
        return BanditModel(arms = arms, featureVersion = featureVersion, rewardVersion = rewardVersion)
    }

    // A draw of the standard normal law, by Box-Muller.
    private fun gaussian(random: Random): Double =
        sqrt(-TWO * ln(1 - random.nextDouble())) * cos(TWO * PI * random.nextDouble())

    private companion object {
        // λ: the weights start near zero, as if each action were worth nothing yet.
        const val PRIOR = 1.0

        // v: how widely the weights are drawn around their mean; the larger, the more the bandit tries.
        const val EXPLORATION = 0.5

        const val DRAWS = 200
        const val TWO = 2.0
    }
}
