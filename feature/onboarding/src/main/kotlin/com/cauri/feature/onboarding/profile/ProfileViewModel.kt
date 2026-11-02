package com.cauri.feature.onboarding.profile

import androidx.lifecycle.ViewModel
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Profile step of the onboarding: first name, occupation and income type, all required. It saves nothing: the payment
 * habits step saves the whole profile, so that a saved profile is always complete.
 */
@HiltViewModel
internal class ProfileViewModel @Inject constructor() : ViewModel() {
    private val state = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = state.asStateFlow()

    // Missing answers are only pointed out once the user tried to continue, then follow what they fill in.
    private var continueAttempted = false

    fun onFirstNameChange(firstName: String) = updateAnswers { it.copy(firstName = firstName) }

    fun onOccupationSelect(occupation: Occupation) = updateAnswers { it.copy(occupation = occupation) }

    fun onIncomeTypeSelect(incomeType: IncomeType) = updateAnswers { it.copy(incomeType = incomeType) }

    /** Gives the answers to hand on when every one is there, otherwise points out the missing ones. */
    fun onContinue() {
        continueAttempted = true
        state.update { current ->
            val missing = missingFields(current)
            val occupation = current.occupation
            val incomeType = current.incomeType
            val answered = if (missing.isEmpty() && occupation != null && incomeType != null) {
                ProfileAnswers(displayName = current.firstName.trim(), occupation = occupation, incomeType = incomeType)
            } else {
                null
            }
            current.copy(missingFields = missing, answered = answered)
        }
    }

    /** The screen handed the answers on: coming back to this step must not hand them on again by itself. */
    fun onAnswersHandedOn() = state.update { it.copy(answered = null) }

    private fun updateAnswers(change: (ProfileUiState) -> ProfileUiState) = state.update { current ->
        val changed = change(current)
        val missing = missingFields(changed)
        changed.copy(
            complete = missing.isEmpty(),
            missingFields = if (continueAttempted) missing else changed.missingFields,
        )
    }

    private fun missingFields(answers: ProfileUiState): Set<ProfileField> = buildSet {
        if (answers.firstName.isBlank()) add(ProfileField.FIRST_NAME)
        if (answers.occupation == null) add(ProfileField.OCCUPATION)
        if (answers.incomeType == null) add(ProfileField.INCOME_TYPE)
    }
}
