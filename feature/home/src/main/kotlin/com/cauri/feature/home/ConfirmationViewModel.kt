package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.ConfirmationRepository
import com.cauri.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « À confirmer »: the cards of the operations read in messages, pre-filled with Cauri's proposal. ✓ or a swipe right
 * confirms, a swipe left sets aside (« Rien dépensé »), each with a short undo.
 */
// One function per gesture of the cards.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class ConfirmationViewModel @Inject constructor(
    transactions: TransactionRepository,
    private val categories: CategoryRepository,
    private val confirmations: ConfirmationRepository,
) : ViewModel() {
    private val state = MutableStateFlow(ConfirmationUiState())

    val uiState: StateFlow<ConfirmationUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            transactions.pending.collect { pending ->
                val known = state.value.cards.associateBy { it.operation.transactionId }
                // The card just answered stays out until its answer is saved, or undone.
                val answered = state.value.answered?.transactionId
                val cards = pending.filter { it.transactionId != answered }.map { operation ->
                    known[operation.transactionId]?.copy(operation = operation) ?: ConfirmationCard(
                        operation = operation,
                        suggested = suggestions(operation.accountId, operation.type),
                        categoryId = null,
                        amount = operation.amount,
                    ).let { it.copy(categoryId = it.suggested.firstOrNull()?.id) }
                }
                state.update { it.copy(cards = cards) }
            }
        }
    }

    /** Chooses the category [categoryId] for the card [transactionId], or none on a second tap. */
    fun onCategory(transactionId: Long, categoryId: Long) = updateCard(transactionId) {
        it.copy(categoryId = categoryId.takeIf { chosen -> chosen != it.categoryId })
    }

    /** Confirms the card [transactionId]; one still without amount, paid in another currency, asks for it first. */
    fun onConfirm(transactionId: Long) {
        val card = card(transactionId) ?: return
        if (card.amount <= 0) {
            state.update { it.copy(editingAmount = transactionId) }
            return
        }
        answer(transactionId = transactionId, confirmed = true)
        viewModelScope.launch {
            confirmations.confirm(
                operation = card.operation,
                categoryId = card.categoryId,
                proposedCategoryId = card.suggested.firstOrNull()?.id,
                amount = card.amount,
            )
        }
    }

    /** Sets the card [transactionId] aside: « Rien dépensé ». */
    fun onDismiss(transactionId: Long) {
        val card = card(transactionId) ?: return
        answer(transactionId = transactionId, confirmed = false)
        viewModelScope.launch { confirmations.dismiss(transactionId = transactionId, type = card.operation.type) }
    }

    /** Opens the field of the amount of the card [transactionId], or closes it with `null`. */
    fun onEditAmount(transactionId: Long?) = state.update { it.copy(editingAmount = transactionId) }

    /** Keeps [amount], in FCFA, for the card being edited. */
    fun onAmount(amount: Long) {
        val transactionId = state.value.editingAmount ?: return
        updateCard(transactionId) { it.copy(amount = amount) }
        state.update { it.copy(editingAmount = null) }
    }

    /** Undoes the last answer: the card waits again, as read in its message. */
    fun onUndo() {
        val answered = state.value.answered ?: return
        state.update { it.copy(answered = null) }
        viewModelScope.launch { confirmations.reopen(answered.transactionId) }
    }

    fun onUndoShown() = state.update { it.copy(answered = null) }

    private suspend fun suggestions(accountId: Long, type: PendingType): List<Category> = when (type) {
        PendingType.EXPENSE -> categories.suggested(
            accountId = accountId,
            type = EntryType.EXPENSE,
            limit = SUGGESTIONS,
        )
        PendingType.INCOME -> categories.suggested(accountId = accountId, type = EntryType.INCOME, limit = SUGGESTIONS)
        PendingType.TRANSFER -> emptyList()
    }

    // The card leaves at once; the list follows once the answer is saved.
    private fun answer(transactionId: Long, confirmed: Boolean) = state.update { current ->
        current.copy(
            cards = current.cards.filterNot { it.operation.transactionId == transactionId },
            answered = AnsweredCard(transactionId = transactionId, confirmed = confirmed),
        )
    }

    private fun card(transactionId: Long) = state.value.cards.firstOrNull {
        it.operation.transactionId == transactionId
    }

    private fun updateCard(transactionId: Long, transform: (ConfirmationCard) -> ConfirmationCard) = state.update {
        it.copy(
            cards = it.cards.map { card ->
                if (card.operation.transactionId ==
                    transactionId
                ) {
                    transform(card)
                } else {
                    card
                }
            },
        )
    }

    private companion object {
        const val SUGGESTIONS = 3
    }
}
