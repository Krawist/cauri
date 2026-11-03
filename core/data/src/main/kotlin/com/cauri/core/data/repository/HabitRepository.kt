package com.cauri.core.data.repository

import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.NewHabit
import kotlinx.coroutines.flow.Flow

/** The user's habitual cash expenses, in the order of the home screen's row. */
interface HabitRepository {
    /** The habits in the order of the row, again after each change. */
    val habits: Flow<List<Habit>>

    /** Adds [habits] at the end of the row, in their order, all or none. */
    suspend fun createHabits(habits: List<NewHabit>)

    /** Changes the name, amount and category of [habit]; its place in the row stays. */
    suspend fun updateHabit(habit: Habit)

    suspend fun deleteHabit(id: Long)

    /** Puts the habits in the order of [ids], at once. */
    suspend fun reorderHabits(ids: List<Long>)
}
