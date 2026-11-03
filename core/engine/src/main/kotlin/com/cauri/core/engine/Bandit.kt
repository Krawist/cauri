package com.cauri.core.engine

import kotlin.random.Random

/**
 * The contextual bandit of the reminders: linear Thompson sampling over the three actions. It learns, from the log,
 * the reward of each action in a context, and chooses by drawing what each may be worth: the best known most often,
 * the others sometimes, to keep learning.
 */
interface Bandit {
    /**
     * The model learnt from [samples], each of [size] features; `null` while too few decisions are valid: 150 at
     * least, and 15 for each action, the rules deciding until then.
     */
    fun train(samples: List<TrainingSample>, size: Int): BanditModel?

    /** The action of [model] for [features], among [allowed], drawn with [random]; and how often it would be. */
    fun choose(model: BanditModel, features: List<Double>, allowed: Set<ReminderAction>, random: Random): BanditChoice

    /** [model] as numbers, to be stored: for each action in order, its decisions, its precision, its rewards. */
    fun encode(model: BanditModel): List<Double>

    /** The model stored as [parameters] by [encode], of [size] features, learnt with these versions. */
    fun decode(parameters: List<Double>, size: Int, featureVersion: Int, rewardVersion: String): BanditModel

    companion object {
        /** Fewest valid decisions before the bandit takes over from the rules. */
        const val MIN_DECISIONS = 150

        /** Fewest valid decisions of each action before the bandit takes over. */
        const val MIN_PER_ACTION = 15
    }
}
