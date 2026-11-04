package com.cauri.core.data.repository

import com.cauri.core.data.model.GoalsOverview
import com.cauri.core.data.model.NewGoal
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [GoalRepository] for tests: the test sets the overview; the changes are kept. */
class FakeGoalRepository : GoalRepository {
    override val overview = MutableStateFlow(GoalsOverview(savedThisMonth = 0, goals = emptyList()))

    val added = mutableListOf<NewGoal>()

    val updated = mutableListOf<Pair<Long, NewGoal>>()

    val deleted = mutableListOf<Long>()

    val movedFirst = mutableListOf<Long>()

    val putAside = mutableListOf<Pair<Long, Long>>()

    val emergencyFunds = mutableListOf<String>()

    override suspend fun add(goal: NewGoal) {
        added += goal
    }

    override suspend fun update(id: Long, goal: NewGoal) {
        updated += id to goal
    }

    override suspend fun delete(id: Long) {
        deleted += id
    }

    override suspend fun moveFirst(id: Long) {
        movedFirst += id
    }

    override suspend fun putAside(id: Long, amount: Long) {
        putAside += id to amount
    }

    override suspend fun createEmergencyFund(name: String) {
        emergencyFunds += name
    }
}
