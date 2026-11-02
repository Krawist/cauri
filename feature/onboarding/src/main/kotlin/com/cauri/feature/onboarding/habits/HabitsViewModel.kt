package com.cauri.feature.onboarding.habits

import androidx.lifecycle.ViewModel
import com.cauri.core.data.model.PaymentHabit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Up to 999 999 999 F: more than enough for cash in pocket, and far from overflowing a Long.
private const val MAX_CASH_DIGITS = 9

/**
 * Payment habits step of the onboarding: the main way of paying and the cash in pocket, both required. It saves
 * nothing: the onboarding saves every answer at its end.
 */
@HiltViewModel
internal class HabitsViewModel @Inject constructor() : ViewModel() {
    private val state = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = state.asStateFlow()

    // Missing answers are only pointed out once the user tried to continue, then follow what they fill in.
    private var continueAttempted = false

    fun onPaymentHabitSelect(habit: PaymentHabit) = updateAnswers { it.copy(paymentHabit = habit) }

    /** Keeps the digits of [text], without leading zeros, up to [MAX_CASH_DIGITS]. */
    fun onCashAmountChange(text: String) {
        val digits = text.filter(Char::isDigit).take(MAX_CASH_DIGITS)
        val withoutLeadingZeros = digits.trimStart('0').ifEmpty { digits.take(1) }
        updateAnswers { it.copy(cashDigits = withoutLeadingZeros) }
    }

    /** Gives the answers to hand on when both are there, otherwise points out the missing ones. */
    fun onContinue() {
        continueAttempted = true
        state.update { current ->
            val habit = current.paymentHabit
            val cash = current.cashDigits.toLongOrNull()
            val answered = if (habit != null &&
                cash != null
            ) {
                HabitsAnswers(paymentHabit = habit, cashAmount = cash)
            } else {
                null
            }
            current.copy(missingFields = missingFields(current), answered = answered)
        }
    }

    /** The screen handed the answers on: coming back to this step must not hand them on again by itself. */
    fun onAnswersHandedOn() = state.update { it.copy(answered = null) }

    private fun updateAnswers(change: (HabitsUiState) -> HabitsUiState) = state.update { current ->
        val changed = change(current)
        val missing = missingFields(changed)
        changed.copy(
            complete = missing.isEmpty(),
            missingFields = if (continueAttempted) missing else changed.missingFields,
        )
    }

    private fun missingFields(answers: HabitsUiState): Set<HabitsField> = buildSet {
        if (answers.paymentHabit == null) add(HabitsField.PAYMENT_HABIT)
        if (answers.cashDigits.isEmpty()) add(HabitsField.CASH_AMOUNT)
    }
}
