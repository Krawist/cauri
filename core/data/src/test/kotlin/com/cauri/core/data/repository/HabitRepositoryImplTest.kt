package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.NewHabit
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class HabitRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: HabitRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = HabitRepositoryImpl(database.habitDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class CreateHabits {
        @Test
        fun `should add the habits at the end of the row, in their order`() = runTest {
            // given
            repository.createHabits(listOf(NewHabit(name = "Moto", amount = 500, categoryId = null)))

            // when
            repository.createHabits(
                listOf(
                    NewHabit(name = "Beignets", amount = 300, categoryId = null),
                    NewHabit(name = "Taxi", amount = 350, categoryId = null),
                ),
            )

            // then
            repository.habits.first().map { it.name } shouldContainExactly listOf("Moto", "Beignets", "Taxi")
        }
    }

    @Nested
    inner class UpdateHabit {
        @Test
        fun `should change the habit and keep its place`() = runTest {
            // given
            repository.createHabits(
                listOf(
                    NewHabit(name = "Moto", amount = 500, categoryId = null),
                    NewHabit(name = "Beignets", amount = 300, categoryId = null),
                ),
            )
            val moto = repository.habits.first().first()

            // when
            repository.updateHabit(moto.copy(name = "Moto-taxi", amount = 600))

            // then
            repository.habits.first() shouldContainExactly listOf(
                Habit(id = moto.id, name = "Moto-taxi", amount = 600, categoryId = null),
                repository.habits.first()[1],
            )
        }
    }

    @Nested
    inner class DeleteHabit {
        @Test
        fun `should remove the habit from the row`() = runTest {
            // given
            repository.createHabits(
                listOf(
                    NewHabit(name = "Moto", amount = 500, categoryId = null),
                    NewHabit(name = "Beignets", amount = 300, categoryId = null),
                ),
            )
            val moto = repository.habits.first().first()

            // when
            repository.deleteHabit(moto.id)

            // then
            repository.habits.first().map { it.name } shouldContainExactly listOf("Beignets")
        }
    }

    @Nested
    inner class ReorderHabits {
        @Test
        fun `should put the habits in the given order`() = runTest {
            // given
            repository.createHabits(
                listOf(
                    NewHabit(name = "Moto", amount = 500, categoryId = null),
                    NewHabit(name = "Beignets", amount = 300, categoryId = null),
                    NewHabit(name = "Taxi", amount = 350, categoryId = null),
                ),
            )
            val (moto, beignets, taxi) = repository.habits.first()

            // when
            repository.reorderHabits(listOf(taxi.id, moto.id, beignets.id))

            // then
            repository.habits.first().map { it.name } shouldContainExactly listOf("Taxi", "Moto", "Beignets")
        }
    }
}
