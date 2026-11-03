package com.cauri.core.data.model

import java.time.Instant

/**
 * A model of the reminders bandit as published.
 *
 * @property version Version of the model, one more at each training.
 * @property trainedAt When it was trained.
 * @property featureVersion Layout of the features it learnt from.
 * @property rewardVersion Rewards it learnt with.
 * @property lastDecisionId Newest decision it learnt from.
 * @property decisions How many decisions it learnt from.
 * @property parameters What it learnt, in the layout of the engine.
 */
data class BanditSnapshot(
    val version: Long,
    val trainedAt: Instant,
    val featureVersion: Int,
    val rewardVersion: String,
    val lastDecisionId: Long,
    val decisions: Int,
    val parameters: List<Double>,
)
