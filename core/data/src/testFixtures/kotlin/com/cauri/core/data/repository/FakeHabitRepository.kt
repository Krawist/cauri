package com.cauri.core.data.repository

import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.NewHabit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [HabitRepository] for tests: created habits get the ids 1, 2, 3… in order. */
class FakeHabitRepository : HabitRepository {
    override val habits = MutableStateFlow<List<Habit>>(emptyList())

    override suspend fun createHabits(habits: List<NewHabit>) {
        val first = (this.habits.value.maxOfOrNull { it.id } ?: 0) + 1
        this.habits.update { current ->
            current + habits.mapIndexed { index, habit ->
                Habit(id = first + index, name = habit.name, amount = habit.amount, categoryId = habit.categoryId)
            }
        }
    }

    override suspend fun updateHabit(habit: Habit) {
        habits.update { current -> current.map { if (it.id == habit.id) habit else it } }
    }

    override suspend fun deleteHabit(id: Long) {
        habits.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun reorderHabits(ids: List<Long>) {
        habits.update { current -> ids.map { id -> current.first { it.id == id } } }
    }
}
