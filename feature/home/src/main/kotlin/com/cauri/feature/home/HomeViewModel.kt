package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.NewHabit
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.HabitRepository
import com.cauri.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Home screen, cash first: the greeting, the estimated cash in pocket, the one-tap cash expenses, the balances of the
 * digital accounts; the operations to confirm have their own [ConfirmationViewModel].
 */
// Injected repositories: one per kind of data the screen shows or changes.
@Suppress("LongParameterList")
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
    cashCounts: CashCountRepository,
    categories: CategoryRepository,
    accounts: AccountRepository,
    private val habits: HabitRepository,
    private val transactions: TransactionRepository,
    private val clock: Clock,
) : ViewModel(),
    HabitActions {
    private val state = MutableStateFlow(HomeUiState(today = LocalDate.now(clock)))

    val uiState: StateFlow<HomeUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.profile,
                cashCounts.balance,
                habits.habits,
                categories.categories,
            ) { profile, cash, habits, categories ->
                state.update { current ->
                    current.copy(
                        name = profile?.displayName.orEmpty(),
                        initial = profile?.displayName?.take(1)?.uppercase().orEmpty(),
                        cash = cash,
                        habits = habits,
                        categories = categories.filter { it.nature == CategoryNature.EXPENSE },
                    )
                }
            }.collect {}
        }
        viewModelScope.launch {
            accounts.balances.collect { balances -> state.update { it.copy(balances = balances) } }
        }
    }

    override fun onHabitTap(habit: Habit) {
        viewModelScope.launch {
            val id = transactions.recordCashExpense(
                amount = habit.amount,
                categoryId = habit.categoryId,
                note = habit.name,
                occurredAt = clock.instant(),
            )
            state.update {
                it.copy(recorded = RecordedHabit(transactionId = id, name = habit.name, amount = habit.amount))
            }
        }
    }

    override fun onEditHabit(habit: Habit?) = state.update {
        it.copy(
            editing = habit?.let { habit ->
                HabitDraft(
                    id = habit.id,
                    name = habit.name,
                    amount = habit.amount.toString(),
                    categoryId = habit.categoryId,
                )
            } ?: HabitDraft(),
        )
    }

    override fun onUndo() {
        val recorded = state.value.recorded ?: return
        state.update { it.copy(recorded = null) }
        viewModelScope.launch { transactions.deleteTransaction(recorded.transactionId) }
    }

    override fun onUndoDismissed() = state.update { it.copy(recorded = null) }

    override fun onDraftChange(draft: HabitDraft) = state.update {
        it.copy(editing = draft.copy(amount = draft.amount.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS)))
    }

    override fun onDraftSave() {
        val draft = state.value.editing ?: return
        val name = draft.name.trim()
        val amount = draft.amount.toLongOrNull() ?: 0
        if (name.isEmpty() || amount <= 0) {
            state.update { it.copy(editing = draft.copy(nameMissing = name.isEmpty(), amountMissing = amount <= 0)) }
            return
        }
        state.update { it.copy(editing = null) }
        viewModelScope.launch {
            if (draft.id == null) {
                habits.createHabits(listOf(NewHabit(name = name, amount = amount, categoryId = draft.categoryId)))
            } else {
                habits.updateHabit(Habit(id = draft.id, name = name, amount = amount, categoryId = draft.categoryId))
            }
        }
    }

    override fun onDraftDelete() {
        val id = state.value.editing?.id ?: return
        state.update { it.copy(editing = null) }
        viewModelScope.launch { habits.deleteHabit(id) }
    }

    override fun onEditClose() = state.update { it.copy(editing = null) }

    override fun onManageHabits(open: Boolean) = state.update {
        if (open) it.copy(editing = null, managing = true) else it.copy(managing = false)
    }

    override fun onReorder(ids: List<Long>) {
        viewModelScope.launch { habits.reorderHabits(ids) }
    }

    private companion object {
        // Up to 9 999 999 F: more than any habit.
        const val MAX_AMOUNT_DIGITS = 7
    }
}
