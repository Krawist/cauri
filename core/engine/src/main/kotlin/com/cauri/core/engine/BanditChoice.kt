package com.cauri.core.engine

/**
 * The action the bandit chose.
 *
 * @property action Action chosen.
 * @property probability How often the bandit chooses it in the same context, from 0 to 1, logged with it.
 */
data class BanditChoice(val action: ReminderAction, val probability: Double)
