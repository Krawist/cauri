package com.cauri.feature.onboarding.habits

import com.cauri.core.data.model.PaymentHabit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class HabitsViewModelTest {
    private val viewModel = HabitsViewModel()

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
        fun `should be complete once the amount is typed after the payment habit`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)

            // when
            viewModel.onCashAmountChange("7500")

            // then
            viewModel.uiState.value.complete.shouldBeTrue()
        }

        @Test
        fun `should not be complete once the amount is erased`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)
            viewModel.onCashAmountChange("7500")

            // when
            viewModel.onCashAmountChange("")

            // then
            viewModel.uiState.value.complete.shouldBeFalse()
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
            viewModel.onContinue()

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder HabitsField.entries
        }

        @Test
        fun `should give no answers while one is missing`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }

        @Test
        fun `should give the payment habit and the cash in pocket`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.BOTH)
            viewModel.onCashAmountChange("7500")

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe
                HabitsAnswers(paymentHabit = PaymentHabit.BOTH, cashAmount = 7_500)
        }

        @Test
        fun `should accept an empty pocket`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_PHONE)
            viewModel.onCashAmountChange("0")

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe
                HabitsAnswers(paymentHabit = PaymentHabit.MOSTLY_PHONE, cashAmount = 0)
        }
    }

    @Nested
    inner class OnAnswersHandedOn {
        @Test
        fun `should not give the answers again when the user comes back to the step`() {
            // given
            viewModel.onPaymentHabitSelect(PaymentHabit.BOTH)
            viewModel.onCashAmountChange("7500")
            viewModel.onContinue()

            // when
            viewModel.onAnswersHandedOn()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }
    }

    @Nested
    inner class OnPaymentHabitSelect {
        @Test
        fun `should stop pointing out the payment habit once chosen after an attempt`() {
            // given
            viewModel.onContinue()

            // when
            viewModel.onPaymentHabitSelect(PaymentHabit.MOSTLY_CASH)

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder listOf(HabitsField.CASH_AMOUNT)
        }
    }
}
