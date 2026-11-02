package com.cauri.feature.home

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import io.kotest.matchers.shouldBe
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

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Initial {
        @Test
        fun `should show the first letter of the name in capitals`() = runTest {
            // given
            preferences.profile.value = UserProfile(
                displayName = "krawist",
                occupation = Occupation.EMPLOYEE,
                incomeType = IncomeType.FIXED_DATE,
                paymentHabit = PaymentHabit.BOTH,
            )
            val viewModel = HomeViewModel(preferences)

            // when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.initial.collect {} }

            // then
            viewModel.initial.value shouldBe "K"
        }

        @Test
        fun `should be empty before the profile is saved`() = runTest {
            // given
            val viewModel = HomeViewModel(preferences)

            // when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.initial.collect {} }

            // then
            viewModel.initial.value shouldBe ""
        }
    }
}
