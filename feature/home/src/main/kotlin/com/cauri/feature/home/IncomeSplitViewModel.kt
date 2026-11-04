package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.IncomeSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The income arrived that Cauri offers to split, savings first, and its split: the savings rate starts at the user's
 * and moves by 5 % for this income only.
 */
@HiltViewModel
internal class IncomeSplitViewModel @Inject constructor(
    private val repository: IncomeSplitRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val state = MutableStateFlow(IncomeSplitUiState())

    val uiState: StateFlow<IncomeSplitUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.pending.collect { income ->
                state.update { it.copy(income = income, proposal = it.proposal?.takeIf { p -> p.income == income }) }
            }
        }
    }

    /** Opens the split of the income, at the user's savings rate. */
    fun onOpen() {
        val income = state.value.income ?: return
        viewModelScope.launch {
            val proposal = repository.proposal(income, savingsRate = preferences.incomeSplit.first().savingsRate)
            state.update { it.copy(proposal = proposal) }
        }
    }

    /** Puts [step] times 5 % more aside, between 0 and 50 %. */
    fun onSavingsRate(step: Int) {
        val proposal = state.value.proposal ?: return
        val rate = (proposal.savingsRate + step * RATE_STEP).coerceIn(0, MAX_RATE)
        viewModelScope.launch {
            val moved = repository.proposal(proposal.income, savingsRate = rate)
            state.update { it.copy(proposal = moved) }
        }
    }

    /** Keeps the split shown and closes it. */
    fun onSplit() {
        val proposal = state.value.proposal ?: return
        state.update { it.copy(proposal = null) }
        viewModelScope.launch { repository.split(proposal.income.transactionId, proposal.split) }
    }

    /** Offers the income no more: all of it goes to the budgets. */
    fun onDismiss() {
        val income = state.value.income ?: return
        state.update { it.copy(proposal = null) }
        viewModelScope.launch { repository.dismiss(income.transactionId) }
    }

    /** Closes the split without answering: the card stays. */
    fun onClose() = state.update { it.copy(proposal = null) }

    companion object {
        /** Steps of the savings rate, in percent. */
        const val RATE_STEP = 5

        /** Most of an income put aside, in percent. */
        const val MAX_RATE = 50
    }
}
