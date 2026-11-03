package com.cauri.core.engine

/**
 * What the bandit learnt of one action: a Bayesian linear regression of its reward on the features.
 *
 * @property precision The matrix A = λI + Σ x xᵀ of the features of its decisions, row by row.
 * @property rewards The vector b = Σ r x of their rewards.
 * @property decisions How many decisions it learnt from.
 */
data class BanditArm(val precision: List<Double>, val rewards: List<Double>, val decisions: Int)
