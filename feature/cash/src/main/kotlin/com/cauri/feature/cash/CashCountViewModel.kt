package com.cauri.feature.cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.repository.CashCountRepository
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
 * Cash count: the total of the banknotes and coins, or typed, against the estimated cash. With money missing, the
 * screen leads to its split; otherwise the count is saved, a surplus with it as found money.
 */
@HiltViewModel
internal class CashCountViewModel @Inject constructor(
    private val cashCounts: CashCountRepository,
    private val clock: Clock,
) : ViewModel(),
    CashCountActions {
    private val state = MutableStateFlow(CashCountUiState())

    val uiState: StateFlow<CashCountUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val expected = cashCounts.balance.first()?.amount
            update { it.copy(expected = expected) }
        }
    }

    override fun onAdd(denomination: Denomination) = update {
        it.copy(counts = it.counts + (denomination to it.count(denomination) + 1))
    }

    override fun onRemove(denomination: Denomination) = update {
        it.copy(counts = it.counts + (denomination to (it.count(denomination) - 1).coerceAtLeast(0)))
    }

    override fun onClearAll() = update { it.copy(counts = emptyMap(), typedTotal = "") }

    override fun onTypeTotal(typing: Boolean) = update { it.copy(typingTotal = typing) }

    override fun onTypedTotalChange(total: String) = update {
        it.copy(typedTotal = total.filter(Char::isDigit).take(MAX_TOTAL_DIGITS))
    }

    override fun onConfirm() {
        val current = state.value
        val gap = current.gap
        if (gap != null && gap > 0) {
            state.update { it.copy(splitting = true) }
        } else {
            viewModelScope.launch {
                cashCounts.addCount(
                    amount = current.total,
                    countedAt = clock.instant(),
                    expectedAmount = current.expected,
                )
                state.update { it.copy(saved = true) }
            }
        }
    }

    /** The screen opened the split of the gap: coming back must not open it again by itself. */
    fun onSplittingHandedOn() = state.update { it.copy(splitting = false) }

    // Changes the choices, then works out the total and the gap again.
    private fun update(change: (CashCountUiState) -> CashCountUiState) = state.update { current ->
        val changed = change(current)
        val total = if (changed.typingTotal) {
            changed.typedTotal.toLongOrNull() ?: 0
        } else {
            changed.counts.entries.sumOf { (denomination, count) -> denomination.value * count }
        }
        changed.copy(total = total, gap = changed.expected?.let { it - total })
    }

    private fun CashCountUiState.count(denomination: Denomination) = counts[denomination] ?: 0

    private companion object {
        // Up to 99 999 999 F in pocket.
        const val MAX_TOTAL_DIGITS = 8
    }
}
