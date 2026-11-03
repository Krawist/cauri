package com.cauri.feature.cash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.HabitRepository
import com.cauri.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Split of the money missing after a count ([GapSplitRoute]): the habits as moments Cauri noticed, the expenses the
 * user adds, and the rest as « Divers cash ». Only typed amounts count.
 */
@HiltViewModel
internal class GapSplitViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cashCounts: CashCountRepository,
    habits: HabitRepository,
    categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val clock: Clock,
) : ViewModel(),
    GapSplitActions {
    private val counted: Long = checkNotNull(savedState["counted"])
    private val expected: Long = checkNotNull(savedState["expected"])
    private val state = MutableStateFlow(GapSplitUiState(gap = expected - counted, rest = expected - counted))
    private var addedLines = 0

    val uiState: StateFlow<GapSplitUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val since = cashCounts.balance.first()?.countedAt
            val lines = habits.habits.first().map {
                GapLine(
                    key = "habit-${it.id}",
                    name = it.name,
                    fromHabit = true,
                    hint = it.amount,
                    categoryId = it.categoryId,
                    note = it.name,
                )
            }
            update { it.copy(since = since, lines = lines + it.lines) }
        }
        viewModelScope.launch {
            categories.categories.collect { all ->
                state.update {
                    it.copy(categories = all.filter { category -> category.nature == CategoryNature.EXPENSE })
                }
            }
        }
    }

    override fun onAmountChange(key: String, amount: String) = update { current ->
        val digits = amount.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS)
        current.copy(lines = current.lines.map { if (it.key == key) it.copy(amount = digits) else it })
    }

    override fun onPickCategory(open: Boolean) = state.update { it.copy(picking = open) }

    override fun onAddExpense(category: Category) = update { current ->
        val line = GapLine(
            key = "added-${addedLines++}",
            name = category.name,
            fromHabit = false,
            hint = null,
            categoryId = category.id,
            note = null,
        )
        current.copy(lines = current.lines + line, picking = false)
    }

    override fun onSave() {
        val current = state.value
        val expenses = current.lines.mapNotNull { line ->
            line.amount.toLongOrNull()?.takeIf { it > 0 }?.let {
                ReconciledExpense(amount = it, categoryId = line.categoryId, note = line.note)
            }
        }
        val rest = listOfNotNull(
            current.rest.takeIf { it > 0 }?.let { ReconciledExpense(amount = it, categoryId = null, note = null) },
        )
        viewModelScope.launch {
            val now = clock.instant()
            transactions.recordReconciliation(expenses = expenses + rest, countedAt = now)
            cashCounts.addCount(amount = counted, countedAt = now, expectedAmount = expected)
            state.update { it.copy(saved = true) }
        }
    }

    // Changes the lines, then works out what is split and what is left.
    private fun update(change: (GapSplitUiState) -> GapSplitUiState) = state.update { current ->
        val changed = change(current)
        val assigned = changed.lines.sumOf { it.amount.toLongOrNull() ?: 0 }
        changed.copy(assigned = assigned, rest = (changed.gap - assigned).coerceAtLeast(0))
    }

    private companion object {
        // Up to 99 999 999 F, as the count.
        const val MAX_AMOUNT_DIGITS = 8
    }
}
