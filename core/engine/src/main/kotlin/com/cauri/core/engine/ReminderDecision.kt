package com.cauri.core.engine

/**
 * A decision of the reminder engine, as the decision log keeps it.
 *
 * @property action Action decided.
 * @property probability Probability of [action] under the policy that decided, from 0 to 1: 1 for the rules.
 * @property features The context as numbers, in the layout of [featureVersion].
 * @property featureVersion Version of the layout of [features].
 * @property modelVersion Rules or model that decided (« rules-1 »).
 */
data class ReminderDecision(
    val action: ReminderAction,
    val probability: Double,
    val features: List<Double>,
    val featureVersion: Int,
    val modelVersion: String,
)
