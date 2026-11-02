package com.cauri.feature.onboarding.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.feature.onboarding.profile.ProfileAnswers
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

// Up to 999 999 999 F: more than enough for cash in pocket, and far from overflowing a Long.
private const val MAX_CASH_DIGITS = 9

/**
 * Payment habits step of the onboarding: the main way of paying, saved with the profile answers as the whole user
 * profile (the reminder rules use it), and the cash in pocket, saved as the first cash count.
 */
@HiltViewModel
internal class HabitsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val cashCounts: CashCountRepository,
    private val clock: Clock,
) : ViewModel() {
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

    /** Saves the whole profile and the first cash count when both answers are there, otherwise points them out. */
    fun onContinue(profile: ProfileAnswers) {
        continueAttempted = true
        val answers = state.updateAndGet { it.copy(missingFields = missingFields(it)) }
        val habit = answers.paymentHabit
        val cash = answers.cashDigits.toLongOrNull()
        if (habit == null || cash == null) return
        viewModelScope.launch {
            preferences.saveProfile(
                UserProfile(
                    displayName = profile.displayName,
                    occupation = profile.occupation,
                    incomeType = profile.incomeType,
                    paymentHabit = habit,
                ),
            )
            cashCounts.addCount(amount = cash, countedAt = clock.instant(), expectedAmount = null)
            state.update { it.copy(saved = true) }
        }
    }

    private fun updateAnswers(change: (HabitsUiState) -> HabitsUiState) = state.update { current ->
        val changed = change(current)
        if (continueAttempted) changed.copy(missingFields = missingFields(changed)) else changed
    }

    private fun missingFields(answers: HabitsUiState): Set<HabitsField> = buildSet {
        if (answers.paymentHabit == null) add(HabitsField.PAYMENT_HABIT)
        if (answers.cashDigits.isEmpty()) add(HabitsField.CASH_AMOUNT)
    }
}
