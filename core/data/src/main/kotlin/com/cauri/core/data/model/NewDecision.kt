package com.cauri.core.data.model

import java.time.Instant

/**
 * A decision of the reminder engine to log, with the context it was taken in: the bandit learns from the log.
 *
 * @property decidedAt When it was taken.
 * @property transactionId Card the decision is about: an operation read in a message or the card of a visit.
 * @property visitId Visit whose card it is; `null` for an operation read in a message.
 * @property features Context as numbers, in the layout of [featureVersion].
 * @property featureVersion Version of the layout of [features].
 * @property action Action decided.
 * @property probability Probability of [action] under the policy that decided, from 0 to 1.
 * @property modelVersion Rules or model that decided.
 */
data class NewDecision(
    val decidedAt: Instant,
    val transactionId: Long,
    val visitId: Long?,
    val features: List<Double>,
    val featureVersion: Int,
    val action: ReminderAction,
    val probability: Double,
    val modelVersion: String,
)
