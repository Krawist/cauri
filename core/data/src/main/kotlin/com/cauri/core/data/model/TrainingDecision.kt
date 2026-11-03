package com.cauri.core.data.model

/**
 * A decision of the log with its outcome, valid for the training of the bandit.
 *
 * @property id Identifier of the decision, growing with time.
 * @property features The context as numbers, in the layout of [featureVersion].
 * @property featureVersion Version of the layout of [features].
 * @property action Action decided.
 * @property outcome What happened afterwards.
 */
data class TrainingDecision(
    val id: Long,
    val features: List<Double>,
    val featureVersion: Int,
    val action: ReminderAction,
    val outcome: ReminderOutcome,
)
