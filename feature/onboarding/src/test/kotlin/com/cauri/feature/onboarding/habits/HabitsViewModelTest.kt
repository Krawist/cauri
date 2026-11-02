package com.cauri.feature.onboarding.habits

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.feature.onboarding.FakeCashCountRepository
import com.cauri.feature.onboarding.FakeUserPreferencesRepository
import com.cauri.feature.onboarding.profile.ProfileAnswers
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class HabitsViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private val cashCounts = FakeCashCountRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-02T08:30:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: HabitsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HabitsViewModel(preferences, cashCounts, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnCashAmountChange {
        @ParameterizedTest
        @CsvSource("7500, 7500", "'7 500 F', 7500", "007500, 7500", "0, 0", "000, 0", "'', ''", "1234567890, 123456789")
        fun `should keep only the digits of the amount`(typed: String, expected: String) {
            // when
            viewModel.onCashAmountChange(typed)

            // then
            viewModel.uiState.value.cashDigits shouldBe expected
        }

        @Test
        fun `should not point out a missing amount before a first attempt`() {
            // when
            viewModel.onCashAmountChange("")

            // then
            viewModel.uiState.value.missingFields.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnContinue {
        @Test
        fun `should point out both missing answers on a first attempt`() {
            // when
            viewModel.onContinue(profileAnswers())

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder HabitsField.entries
        }

        @Test
        fun `should save nothing while an answer is missing`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)

            // when
            viewModel.onContinue(profileAnswers())

            // then
            preferences.profile.value.shouldBeNull()
            cashCounts.counts.shouldBeEmpty()
        }

        @Test
        fun `should save the whole profile with the payment habit`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.BOTH)
            viewModel.onCashAmountChange("7500")

            // when
            viewModel.onContinue(profileAnswers())

            // then
            preferences.profile.value shouldBe UserProfile(
                displayName = "Krawist",
                occupation = Occupation.TRADER,
                incomeType = IncomeType.VARIABLE,
                paymentHabit = PaymentHabit.BOTH,
            )
        }

        @Test
        fun `should record the cash in pocket as the first cash count`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)
            viewModel.onCashAmountChange("7500")

            // when
            viewModel.onContinue(profileAnswers())

            // then
            viewModel.uiState.value.saved.shouldBeTrue()
            cashCounts.counts shouldContainExactly listOf(
                FakeCashCountRepository.Count(amount = 7_500, countedAt = clock.instant(), expectedAmount = null),
            )
        }

        @Test
        fun `should accept an empty pocket`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_PHONE)
            viewModel.onCashAmountChange("0")

            // when
            viewModel.onContinue(profileAnswers())

            // then
            cashCounts.counts.map { it.amount } shouldContainExactly listOf(0L)
        }
    }

    @Nested
    inner class OnPaymentHabitSelect {
        @Test
        fun `should stop pointing out the payment habit once chosen after an attempt`() {
            // given
            viewModel.onContinue(profileAnswers())

            // when
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder listOf(HabitsField.CASH_AMOUNT)
        }
    }

    // The answers of the profile step, as the navigation hands them on.
    private fun profileAnswers() =
        ProfileAnswers(displayName = "Krawist", occupation = Occupation.TRADER, incomeType = IncomeType.VARIABLE)
}
