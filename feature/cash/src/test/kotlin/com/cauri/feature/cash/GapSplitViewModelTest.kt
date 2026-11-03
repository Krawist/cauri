package com.cauri.feature.cash

import androidx.lifecycle.SavedStateHandle
import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.repository.FakeCashCountRepository
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeHabitRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GapSplitViewModelTest {
    private val cashCounts = FakeCashCountRepository()
    private val habits = FakeHabitRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T20:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: GapSplitViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        cashCounts.balance.value = CashBalance(amount = 7_500, countedAt = Instant.EPOCH, expensesSinceCount = 0)
        habits.habits.value = listOf(Habit(id = 1, name = "Moto", amount = 500, categoryId = 3))
        categories.categories.value = listOf(
            Category(id = 3, name = "Transport", nature = CategoryNature.EXPENSE),
            Category(id = 4, name = "Tontine", nature = CategoryNature.SAVING),
        )
        viewModel = GapSplitViewModel(
            savedState = SavedStateHandle(mapOf("counted" to 3_000L, "expected" to 7_500L)),
            cashCounts = cashCounts,
            habits = habits,
            categories = categories,
            transactions = transactions,
            clock = clock,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should suggest the habits, grey until typed`() {
            // then
            viewModel.uiState.value.lines shouldContainExactly listOf(
                GapLine(key = "habit-1", name = "Moto", fromHabit = true, hint = 500, categoryId = 3, note = "Moto"),
            )
        }

        @Test
        fun `should leave the whole gap unplaced while nothing is typed`() {
            // then
            viewModel.uiState.value.rest shouldBe 4_500
        }

        @Test
        fun `should offer only spending categories to add an expense`() {
            // then
            viewModel.uiState.value.categories.map { it.name } shouldContainExactly listOf("Transport")
        }
    }

    @Nested
    inner class OnAmountChange {
        @Test
        fun `should count the typed amount as split`() {
            // when
            viewModel.onAmountChange(key = "habit-1", amount = "2 000")

            // then
            viewModel.uiState.value.assigned shouldBe 2_000
            viewModel.uiState.value.rest shouldBe 2_500
        }

        @Test
        fun `should never leave less than nothing unplaced`() {
            // when
            viewModel.onAmountChange(key = "habit-1", amount = "6000")

            // then
            viewModel.uiState.value.rest shouldBe 0
        }
    }

    @Nested
    inner class OnAddExpense {
        @Test
        fun `should add a line in the chosen category`() {
            // when
            viewModel.onAddExpense(Category(id = 3, name = "Transport", nature = CategoryNature.EXPENSE))

            // then
            viewModel.uiState.value.lines.last() shouldBe GapLine(
                key = "added-0",
                name = "Transport",
                fromHabit = false,
                hint = null,
                categoryId = 3,
                note = null,
            )
        }
    }

    @Nested
    inner class OnSave {
        @Test
        fun `should record the typed expenses and the rest as miscellaneous cash, at the count`() {
            // given
            viewModel.onAmountChange(key = "habit-1", amount = "2000")

            // when
            viewModel.onSave()

            // then
            transactions.reconciliations shouldContainExactly listOf(
                listOf(
                    ReconciledExpense(amount = 2_000, categoryId = 3, note = "Moto"),
                    ReconciledExpense(amount = 2_500, categoryId = null, note = null),
                ) to clock.instant(),
            )
        }

        @Test
        fun `should save the count, then close`() {
            // when
            viewModel.onSave()

            // then
            cashCounts.counts shouldContainExactly listOf(
                FakeCashCountRepository.Count(amount = 3_000, countedAt = clock.instant(), expectedAmount = 7_500),
            )
            viewModel.uiState.value.saved.shouldBeTrue()
        }
    }
}
