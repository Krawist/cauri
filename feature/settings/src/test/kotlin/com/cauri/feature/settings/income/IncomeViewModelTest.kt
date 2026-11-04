package com.cauri.feature.settings.income

import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.NewFixedCost
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeFixedCostRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class IncomeViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private val fixedCosts = FakeFixedCostRepository()
    private lateinit var viewModel: IncomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = IncomeViewModel(preferences = preferences, fixedCosts = fixedCosts)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnSavingsRate {
        @ParameterizedTest
        @CsvSource("10, 1, 15", "0, -1, 0", "50, 1, 50")
        fun `should save the savings rate moved by 5 percent within its bounds`(rate: Int, step: Int, expected: Int) {
            // given
            preferences.incomeSplit.value = IncomeSplitSettings(savingsRate = rate, threshold = 10_000)

            // when
            viewModel.onSavingsRate(step)

            // then
            preferences.incomeSplit.value.savingsRate shouldBe expected
        }
    }

    @Nested
    inner class OnThreshold {
        @ParameterizedTest
        @CsvSource("10000, 1, 15000", "0, -1, 0", "200000, 1, 200000")
        fun `should save the threshold moved by 5 000 F within its bounds`(threshold: Long, step: Int, expected: Long) {
            // given
            preferences.incomeSplit.value = IncomeSplitSettings(savingsRate = 10, threshold = threshold)

            // when
            viewModel.onThreshold(step)

            // then
            preferences.incomeSplit.value.threshold shouldBe expected
        }
    }

    @Nested
    inner class OnConfirm {
        @Test
        fun `should make the suggestion a fixed cost`() {
            // given
            val cost = NewFixedCost(name = "ENEO", amount = 12_000, dayOfMonth = 7, counterparty = "ENEO")
            fixedCosts.suggestions.value = listOf(FixedCostSuggestion(cost = cost, months = 3))

            // when
            viewModel.onConfirm(fixedCosts.suggestions.value.single())

            // then
            fixedCosts.added shouldContainExactly listOf(cost)
            viewModel.uiState.value.suggestions.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnIgnore {
        @Test
        fun `should refuse the suggestion`() {
            // given
            val cost = NewFixedCost(name = "Canal+", amount = 8_000, dayOfMonth = 3, counterparty = "Canal+")
            fixedCosts.suggestions.value = listOf(FixedCostSuggestion(cost = cost, months = 2))

            // when
            viewModel.onIgnore(fixedCosts.suggestions.value.single())

            // then
            fixedCosts.ignored shouldContainExactly listOf(cost)
        }
    }

    @Nested
    inner class OnDraftSave {
        @Test
        fun `should add the fixed cost typed`() {
            // given
            viewModel.onEdit(null)
            viewModel.onDraftChange(FixedCostDraft(name = " Loyer ", amount = "50 000", dayOfMonth = 5))

            // when
            viewModel.onDraftSave()

            // then
            fixedCosts.added shouldContainExactly listOf(NewFixedCost(name = "Loyer", amount = 50_000, dayOfMonth = 5))
            viewModel.uiState.value.draft.shouldBeNull()
        }

        @Test
        fun `should replace the fixed cost edited, keeping its merchant`() {
            // given
            viewModel.onEdit(fixedCost())
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(amount = "13000"))

            // when
            viewModel.onDraftSave()

            // then
            fixedCosts.updated shouldContainExactly listOf(
                4L to NewFixedCost(name = "ENEO", amount = 13_000, dayOfMonth = 7, counterparty = "ENEO"),
            )
        }

        @Test
        fun `should point out the name and the amount missing`() {
            // given
            viewModel.onEdit(null)

            // when
            viewModel.onDraftSave()

            // then
            viewModel.uiState.value.draft shouldBe FixedCostDraft(nameMissing = true, amountMissing = true)
            fixedCosts.added.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnDraftChange {
        @ParameterizedTest
        @CsvSource("0, 1", "32, 31")
        fun `should keep the day within the month`(day: Int, expected: Int) {
            // given
            viewModel.onEdit(null)

            // when
            viewModel.onDraftChange(FixedCostDraft(dayOfMonth = day))

            // then
            viewModel.uiState.value.draft?.dayOfMonth shouldBe expected
        }
    }

    @Nested
    inner class OnDraftDelete {
        @Test
        fun `should delete the fixed cost edited`() {
            // given
            viewModel.onEdit(fixedCost())

            // when
            viewModel.onDraftDelete()

            // then
            fixedCosts.deleted shouldContainExactly listOf(4L)
            viewModel.uiState.value.draft.shouldBeNull()
        }
    }

    private fun fixedCost() = FixedCost(
        id = 4,
        name = "ENEO",
        amount = 12_000,
        dayOfMonth = 7,
        categoryId = null,
        counterparty = "ENEO",
        paid = false,
    )
}
