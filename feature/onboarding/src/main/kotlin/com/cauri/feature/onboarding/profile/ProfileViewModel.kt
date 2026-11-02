package com.cauri.feature.onboarding.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/** Profile step of the onboarding: first name, occupation and income type, all required. */
@HiltViewModel
internal class ProfileViewModel @Inject constructor(private val preferences: UserPreferencesRepository) : ViewModel() {
    private val state = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = state.asStateFlow()

    // Missing answers are only pointed out once the user tried to continue, then follow what they fill in.
    private var continueAttempted = false

    fun onFirstNameChange(firstName: String) = updateAnswers { it.copy(firstName = firstName) }

    fun onOccupationSelect(occupation: Occupation) = updateAnswers { it.copy(occupation = occupation) }

    fun onIncomeTypeSelect(incomeType: IncomeType) = updateAnswers { it.copy(incomeType = incomeType) }

    /** Saves the profile when every answer is there, otherwise points out the missing ones. */
    fun onContinue() {
        continueAttempted = true
        val answers = state.updateAndGet { it.copy(missingFields = missingFields(it)) }
        val occupation = answers.occupation
        val incomeType = answers.incomeType
        if (answers.missingFields.isNotEmpty() || occupation == null || incomeType == null) return
        viewModelScope.launch {
            preferences.saveProfile(
                UserProfile(displayName = answers.firstName.trim(), occupation = occupation, incomeType = incomeType),
            )
            state.update { it.copy(saved = true) }
        }
    }

    private fun updateAnswers(change: (ProfileUiState) -> ProfileUiState) = state.update { current ->
        val changed = change(current)
        if (continueAttempted) changed.copy(missingFields = missingFields(changed)) else changed
    }

    private fun missingFields(answers: ProfileUiState): Set<ProfileField> = buildSet {
        if (answers.firstName.isBlank()) add(ProfileField.FIRST_NAME)
        if (answers.occupation == null) add(ProfileField.OCCUPATION)
        if (answers.incomeType == null) add(ProfileField.INCOME_TYPE)
    }
}
