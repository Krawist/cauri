package com.cauri.core.engine

/**
 * A decision of the log the bandit learns from.
 *
 * @property features The context as numbers, in the layout of [FeatureEncoder.VERSION].
 * @property action Action taken.
 * @property reward Its reward, from [RewardTable].
 */
data class TrainingSample(val features: List<Double>, val action: ReminderAction, val reward: Double)
