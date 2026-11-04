package com.cauri.core.data.model

/**
 * « Objectifs ».
 *
 * @property savedThisMonth What was put aside this month, in minor units of XAF.
 * @property goals The goals, in their order: the savings of an income go to the first saving not reached.
 */
data class GoalsOverview(val savedThisMonth: Long, val goals: List<GoalProgress>)
