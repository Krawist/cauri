package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A model of the reminders bandit, published at once after a night of training. Only the latest decides; it can be
 * rebuilt from the decision log at any time.
 *
 * @property version Version of the model, one more at each training.
 * @property trainedAt When it was trained.
 * @property featureVersion Layout of the features it learnt from.
 * @property rewardVersion Rewards it learnt with.
 * @property lastDecisionId Newest decision it learnt from: a training on the same log publishes nothing new.
 * @property decisions How many decisions it learnt from.
 * @property parameters What it learnt, in the layout of the engine.
 */
@Entity(tableName = "bandit_models")
internal data class BanditModelEntity(
    @PrimaryKey(autoGenerate = true) val version: Long = 0,
    val trainedAt: Instant,
    val featureVersion: Int,
    val rewardVersion: String,
    val lastDecisionId: Long,
    val decisions: Int,
    val parameters: List<Double>,
)
