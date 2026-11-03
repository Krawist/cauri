package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.HabitDao
import com.cauri.core.data.database.entity.HabitEntity
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.NewHabit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [HabitRepository]. */
@Singleton
internal class HabitRepositoryImpl @Inject constructor(private val habitDao: HabitDao) : HabitRepository {
    override val habits: Flow<List<Habit>> = habitDao.observeAll().map { habits ->
        habits.map { Habit(id = it.id, name = it.name, amount = it.amount, categoryId = it.categoryId) }
    }

    override suspend fun createHabits(habits: List<NewHabit>) {
        val first = habitDao.nextPosition()
        habitDao.insertAll(
            habits.mapIndexed { index, habit ->
                HabitEntity(
                    name = habit.name,
                    amount = habit.amount,
                    categoryId = habit.categoryId,
                    position = first + index,
                )
            },
        )
    }

    override suspend fun updateHabit(habit: Habit) =
        habitDao.update(id = habit.id, name = habit.name, amount = habit.amount, categoryId = habit.categoryId)

    override suspend fun deleteHabit(id: Long) = habitDao.delete(id)

    override suspend fun reorderHabits(ids: List<Long>) = habitDao.reorder(ids)
}
