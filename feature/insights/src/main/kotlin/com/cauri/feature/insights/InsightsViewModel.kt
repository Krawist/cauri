package com.cauri.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « Analyses »: the budgets Cauri suggests from real spending, after four weeks; the user accepts each, adjusts it or
 * ignores it, and the card goes.
 */
@HiltViewModel
internal class InsightsViewModel @Inject constructor(private val budgets: BudgetRepository) : ViewModel() {
    private val state = MutableStateFlow(InsightsUiState())

    val uiState: StateFlow<InsightsUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { state.update { it.copy(suggestions = budgets.suggestions()) } }
    }

    /** Keeps [suggestion] as the budget of its category. */
    fun onAccept(suggestion: BudgetSuggestion) = answer(suggestion) {
        budgets.setBudget(categoryId = it.categoryId, amount = it.amount, suggested = it.amount)
    }

    /** Opens the field of the amount of [suggestion], or closes it with `null`. */
    fun onAdjust(suggestion: BudgetSuggestion?) = state.update { it.copy(adjusting = suggestion) }

    /** Keeps [amount], in FCFA, as the budget of the suggestion being adjusted, next to the one suggested. */
    fun onAmount(amount: Long) {
        val suggestion = state.value.adjusting ?: return
        state.update { it.copy(adjusting = null) }
        answer(suggestion) { budgets.setBudget(categoryId = it.categoryId, amount = amount, suggested = it.amount) }
    }

    /** Ignores [suggestion]: Cauri suggests none again for its category. */
    fun onIgnore(suggestion: BudgetSuggestion) = answer(suggestion) { budgets.ignore(it.categoryId) }

    // The card goes at once; the answer is saved behind.
    private fun answer(suggestion: BudgetSuggestion, save: suspend (BudgetSuggestion) -> Unit) {
        state.update { current -> current.copy(suggestions = current.suggestions - suggestion) }
        viewModelScope.launch { save(suggestion) }
    }
}
