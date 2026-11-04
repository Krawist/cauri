package com.cauri.feature.home

import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.PendingIncome
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeIncomeSplitRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
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
internal class IncomeSplitViewModelTest {
    private val repository = FakeIncomeSplitRepository()
    private val preferences = FakeUserPreferencesRepository()
    private lateinit var viewModel: IncomeSplitViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = IncomeSplitViewModel(repository = repository, preferences = preferences)
        repository.pending.value = INCOME
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnOpen {
        @Test
        fun `should propose the split at the user's savings rate`() {
            // given
            preferences.incomeSplit.value = IncomeSplitSettings(savingsRate = 20, threshold = 10_000)

            // when
            viewModel.onOpen()

            // then
            viewModel.uiState.value.proposal?.split shouldBe
                IncomeSplit(savings = 30_000, fixedCosts = 0, budgets = 120_000)
        }
    }

    @Nested
    inner class OnSavingsRate {
        @ParameterizedTest
        @CsvSource("10, 1, 15", "0, -1, 0", "50, 1, 50")
        fun `should move the savings rate by 5 percent within its bounds`(rate: Int, step: Int, expected: Int) {
            // given
            preferences.incomeSplit.value = IncomeSplitSettings(savingsRate = rate, threshold = 10_000)
            viewModel.onOpen()

            // when
            viewModel.onSavingsRate(step)

            // then
            viewModel.uiState.value.proposal?.savingsRate shouldBe expected
        }

        @Test
        fun `should keep the user's savings rate for the next incomes`() {
            // given
            viewModel.onOpen()

            // when
            viewModel.onSavingsRate(1)

            // then
            preferences.incomeSplit.value.savingsRate shouldBe 10
        }
    }

    @Nested
    inner class OnSplit {
        @Test
        fun `should keep the split shown and close it`() {
            // given
            viewModel.onOpen()

            // when
            viewModel.onSplit()

            // then
            viewModel.uiState.value.proposal.shouldBeNull()
            repository.splits shouldContainExactly
                listOf(7L to IncomeSplit(savings = 15_000, fixedCosts = 0, budgets = 135_000))
        }
    }

    @Nested
    inner class OnDismiss {
        @Test
        fun `should offer the income no more`() {
            // when
            viewModel.onDismiss()

            // then
            viewModel.uiState.value.income.shouldBeNull()
            repository.dismissed shouldContainExactly listOf(7L)
        }
    }

    @Nested
    inner class OnClose {
        @Test
        fun `should close the split and keep the card`() {
            // given
            viewModel.onOpen()

            // when
            viewModel.onClose()

            // then
            viewModel.uiState.value.proposal.shouldBeNull()
            viewModel.uiState.value.income shouldBe INCOME
        }
    }

    private companion object {
        val INCOME = PendingIncome(
            transactionId = 7,
            amount = 150_000,
            accountName = "Orange Money",
            counterparty = null,
            occurredAt = Instant.parse("2026-11-03T09:00:00Z"),
        )
    }
}
