package com.cauri.feature.home

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeCashCountRepository
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private val cashCounts = FakeCashCountRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should show the first letter of the name in capitals`() = runTest {
            // given
            preferences.profile.value = UserProfile(
                displayName = "krawist",
                occupation = Occupation.EMPLOYEE,
                incomeType = IncomeType.FIXED_DATE,
                paymentHabit = PaymentHabit.BOTH,
            )
            val viewModel = HomeViewModel(preferences, cashCounts)

            // when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

            // then
            viewModel.uiState.value.initial shouldBe "K"
        }

        @Test
        fun `should show no initial before the profile is saved`() = runTest {
            // given
            val viewModel = HomeViewModel(preferences, cashCounts)

            // when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

            // then
            viewModel.uiState.value.initial shouldBe ""
        }

        @Test
        fun `should follow the estimated cash in pocket`() = runTest {
            // given
            val viewModel = HomeViewModel(preferences, cashCounts)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
            val balance = CashBalance(amount = 7_000, countedAt = Instant.ofEpochSecond(1_000), expensesSinceCount = 1)

            // when
            cashCounts.balance.value = balance

            // then
            viewModel.uiState.value.cash shouldBe balance
        }
    }
}
