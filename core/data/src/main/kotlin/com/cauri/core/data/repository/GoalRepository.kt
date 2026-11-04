package com.cauri.core.data.repository

import com.cauri.core.data.model.GoalsOverview
import com.cauri.core.data.model.NewGoal
import kotlinx.coroutines.flow.Flow

/** The goals of the user: savings, tontines and envelopes, and the money put aside for them. */
interface GoalRepository {
    /** What was put aside this month and where each goal stands, again after each change. */
    val overview: Flow<GoalsOverview>

    /** Saves [goal] at the end of the list; a tontine's contribution becomes a fixed cost. */
    suspend fun add(goal: NewGoal)

    /** Replaces the goal [id] by [goal], its kind kept, and the fixed cost of a tontine with it. */
    suspend fun update(id: Long, goal: NewGoal)

    /** Deletes the goal [id], what was put aside for it, and the fixed cost of a tontine. */
    suspend fun delete(id: Long)

    /** Puts the goal [id] first: the next savings go to it until it is reached. */
    suspend fun moveFirst(id: Long)

    /** Puts [amount] aside for the saving [id] by hand, in minor units of XAF; a negative amount takes it back. */
    suspend fun putAside(id: Long, amount: Long)

    /**
     * Creates once, first, the emergency fund named [name], without deadline: a month of spending (the last 30 days,
     * rounded up to 5 000 F), 50 000 F before any spending. Deleted, it is never created again.
     */
    suspend fun createEmergencyFund(name: String)
}
