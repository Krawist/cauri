package com.cauri.feature.insights

import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.data.repository.FakeBudgetRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
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
internal class InsightsViewModelTest {
    private val budgets = FakeBudgetRepository()
    private lateinit var viewModel: InsightsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        budgets.suggested = listOf(food(), transport())
        viewModel = InsightsViewModel(budgets)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnAccept {
        @Test
        fun `should keep the suggestion as the budget, the card gone`() {
            // when
            viewModel.onAccept(food())

            // then
            budgets.set shouldContainExactly
                listOf(FakeBudgetRepository.Set(categoryId = 1, amount = 52_000, suggested = 52_000))
            viewModel.uiState.value.suggestions shouldContainExactly listOf(transport())
        }
    }

    @Nested
    inner class OnAmount {
        @Test
        fun `should keep the amount typed next to the one suggested`() {
            // given
            viewModel.onAdjust(food())

            // when
            viewModel.onAmount(55_000)

            // then
            budgets.set shouldContainExactly
                listOf(FakeBudgetRepository.Set(categoryId = 1, amount = 55_000, suggested = 52_000))
        }
    }

    @Nested
    inner class OnIgnore {
        @Test
        fun `should ignore the suggestion`() {
            // when
            viewModel.onIgnore(transport())

            // then
            budgets.ignored shouldContainExactly listOf(2L)
            budgets.set.shouldBeEmpty()
        }
    }

    private fun food() = BudgetSuggestion(categoryId = 1, categoryName = "Alimentation", amount = 52_000)

    private fun transport() = BudgetSuggestion(categoryId = 2, categoryName = "Transport", amount = 28_000)
}
