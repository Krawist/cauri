package com.cauri.feature.settings.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.NewFixedCost
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.FixedCostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** « Répartir mes revenus »: the savings rate, the smallest income split, and the fixed costs. */
@HiltViewModel
internal class IncomeViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val fixedCosts: FixedCostRepository,
) : ViewModel() {
    private val draft = MutableStateFlow<FixedCostDraft?>(null)

    val uiState: StateFlow<IncomeUiState> = combine(
        preferences.incomeSplit,
        fixedCosts.fixedCosts,
        fixedCosts.suggestions,
        draft,
    ) { settings, costs, suggestions, draft ->
        IncomeUiState(settings = settings, fixedCosts = costs, suggestions = suggestions, draft = draft)
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = IncomeUiState())

    fun onSavingsRate(step: Int) = saveSettings {
        it.copy(savingsRate = (it.savingsRate + step * RATE_STEP).coerceIn(0, MAX_RATE))
    }

    fun onThreshold(step: Int) = saveSettings {
        it.copy(threshold = (it.threshold + step * THRESHOLD_STEP).coerceIn(0, MAX_THRESHOLD))
    }

    fun onConfirm(suggestion: FixedCostSuggestion) {
        viewModelScope.launch { fixedCosts.add(suggestion.cost) }
    }

    fun onIgnore(suggestion: FixedCostSuggestion) {
        viewModelScope.launch { fixedCosts.ignore(suggestion.cost) }
    }

    fun onEdit(cost: FixedCost?) {
        draft.value = cost?.let {
            FixedCostDraft(
                id = it.id,
                name = it.name,
                amount = it.amount.toString(),
                dayOfMonth = it.dayOfMonth,
                categoryId = it.categoryId,
                counterparty = it.counterparty,
            )
        } ?: FixedCostDraft()
    }

    fun onDraftChange(changed: FixedCostDraft) {
        draft.value = changed.copy(
            amount = changed.amount.filter(Char::isDigit).trimStart('0').take(MAX_AMOUNT_DIGITS),
            dayOfMonth = changed.dayOfMonth.coerceIn(1, LAST_DAY),
        )
    }

    /** Saves the fixed cost in the sheet, or points out its name or amount missing. */
    fun onDraftSave() {
        val current = draft.value ?: return
        val amount = current.amount.toLongOrNull() ?: 0
        if (current.name.isBlank() || amount <= 0) {
            draft.value = current.copy(nameMissing = current.name.isBlank(), amountMissing = amount <= 0)
            return
        }
        draft.value = null
        val cost = NewFixedCost(
            name = current.name.trim(),
            amount = amount,
            dayOfMonth = current.dayOfMonth,
            categoryId = current.categoryId,
            counterparty = current.counterparty,
        )
        viewModelScope.launch {
            val id = current.id
            if (id == null) fixedCosts.add(cost) else fixedCosts.update(id, cost)
        }
    }

    fun onDraftDelete() {
        val id = draft.value?.id ?: return
        draft.value = null
        viewModelScope.launch { fixedCosts.delete(id) }
    }

    fun onEditClose() {
        draft.value = null
    }

    private fun saveSettings(change: (IncomeSplitSettings) -> IncomeSplitSettings) {
        viewModelScope.launch { preferences.saveIncomeSplit(change(preferences.incomeSplit.first())) }
    }

    companion object {
        /** Steps of the savings rate, in percent. */
        const val RATE_STEP = 5

        /** Most of an income put aside, in percent. */
        const val MAX_RATE = 50

        /** Steps of the smallest income split, in FCFA. */
        const val THRESHOLD_STEP = 5_000L

        /** Highest smallest income split, in FCFA. */
        const val MAX_THRESHOLD = 200_000L

        // Up to 99 999 999 F, as the cash count.
        private const val MAX_AMOUNT_DIGITS = 8
        private const val LAST_DAY = 31
    }
}
