package com.cauri.core.engine

/**
 * A model of the bandit, one regression per action, as it is published and rebuilt from the decision log.
 *
 * @property arms What was learnt of each action.
 * @property featureVersion Layout of the features it learnt from ([FeatureEncoder.VERSION]).
 * @property rewardVersion Rewards it learnt with ([RewardTable.VERSION]).
 */
data class BanditModel(val arms: Map<ReminderAction, BanditArm>, val featureVersion: Int, val rewardVersion: String)
