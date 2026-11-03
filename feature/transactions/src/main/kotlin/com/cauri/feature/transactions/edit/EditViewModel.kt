package com.cauri.feature.transactions.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.SplitPart
import com.cauri.core.data.model.TransactionEdit
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.ConfirmationRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.feature.transactions.entry.QuickEntryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Edit sheet of a transaction: its amount, category (Cauri's proposal first), account, time and note, and the parts
 * split to other categories. « Valider » confirms it as edited, « Rien dépensé » sets it aside.
 */
// One function per gesture of the sheet.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class EditViewModel @Inject constructor(
    accounts: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactions: TransactionRepository,
    private val confirmations: ConfirmationRepository,
) : ViewModel(),
    EditActions {
    private val state = MutableStateFlow(EditUiState())

    val uiState: StateFlow<EditUiState> = state.asStateFlow()

    // Every active category, by name: the type of the transaction picks those it shows.
    private var allCategories = emptyList<Category>()

    init {
        viewModelScope.launch {
            combine(accounts.accounts, categoryRepository.categories) { accounts, categories ->
                allCategories = categories
                change { it.copy(accounts = accounts, categories = categoriesOf(it.transaction?.type)) }
            }.collect {}
        }
    }

    /**
     * Loads the transaction [transactionId] and the categories Cauri suggests for it, the first chosen if none was.
     * What the user typed is kept when the sheet opens again on the same transaction, as after the screen turned or
     * changed theme.
     */
    fun onOpen(transactionId: Long) {
        if (state.value.transaction?.transactionId == transactionId) return
        viewModelScope.launch {
            val transaction = transactions.getEditable(transactionId) ?: return@launch
            val type = when (transaction.type) {
                PendingType.EXPENSE -> EntryType.EXPENSE
                PendingType.INCOME -> EntryType.INCOME
                PendingType.TRANSFER -> null
            }
            val suggested = type?.let {
                categoryRepository.suggested(accountId = transaction.accountId, type = it, limit = SUGGESTIONS)
            }.orEmpty()
            change {
                EditUiState(
                    transaction = transaction,
                    amount = transaction.amount.takeIf { amount -> amount > 0 }?.toString().orEmpty(),
                    accounts = it.accounts,
                    accountId = transaction.accountId,
                    categories = categoriesOf(transaction.type),
                    suggested = suggested,
                    categoryId = transaction.categoryId ?: suggested.firstOrNull()?.id,
                    occurredAt = transaction.occurredAt,
                    note = transaction.note.orEmpty(),
                )
            }
        }
    }

    /** Forgets the transaction and what was typed, once the sheet closed, answered or not. */
    fun onClose() = change { EditUiState(accounts = it.accounts) }

    override fun onAmount(digits: String) {
        val amount = digits.filter(Char::isDigit).trimStart('0')
        if ((amount.toLongOrNull() ?: 0) > QuickEntryViewModel.MAX_AMOUNT) {
            change { it.copy(amountTooHigh = true) }
        } else {
            change { it.copy(amount = amount, amountTooHigh = false) }
        }
    }

    override fun onCategory(id: Long) = change { it.copy(categoryId = id.takeIf { chosen -> chosen != it.categoryId }) }

    override fun onShowAllCategories() = change { it.copy(allCategoriesShown = true) }

    override fun onAccount(id: Long) = change { if (it.transaction?.read == false) it.copy(accountId = id) else it }

    override fun onOccurredAt(moment: Instant) = change { it.copy(occurredAt = moment) }

    override fun onNote(note: String) = change { it.copy(note = note) }

    override fun onAddPart() = change { it.copy(parts = it.parts + PartDraft()) }

    override fun onPartCategory(index: Int, categoryId: Long) = changePart(index) { it.copy(categoryId = categoryId) }

    override fun onPartAmount(index: Int, digits: String) =
        changePart(index) { it.copy(amount = digits.filter(Char::isDigit).trimStart('0').take(MAX_DIGITS)) }

    override fun onRemovePart(index: Int) =
        change { it.copy(parts = it.parts.filterIndexed { position, _ -> position != index }) }

    override fun onConfirm() {
        val edit = state.value
        val transaction = edit.transaction
        val accountId = edit.accountId
        if (!edit.canConfirm || transaction == null || accountId == null) return
        change { it.copy(closed = true) }
        viewModelScope.launch {
            confirmations.confirm(
                TransactionEdit(
                    transactionId = transaction.transactionId,
                    amount = edit.amount.toLong(),
                    categoryId = edit.categoryId,
                    // The first category suggested is Cauri's proposal, kept whatever the user chose.
                    proposedCategoryId = edit.suggested.firstOrNull()?.id,
                    accountId = accountId,
                    occurredAt = edit.occurredAt,
                    note = edit.note,
                    parts = edit.parts.mapNotNull { part ->
                        part.categoryId?.let { SplitPart(categoryId = it, amount = part.amount.toLong()) }
                    },
                ),
            )
        }
    }

    override fun onDismiss() {
        val transaction = state.value.transaction ?: return
        change { it.copy(closed = true) }
        viewModelScope.launch {
            confirmations.dismiss(transactionId = transaction.transactionId, type = transaction.type)
        }
    }

    private fun categoriesOf(type: PendingType?): List<Category> = when (type) {
        PendingType.EXPENSE -> allCategories.filter { it.nature == CategoryNature.EXPENSE }
        PendingType.INCOME -> allCategories.filter { it.nature == CategoryNature.INCOME }
        PendingType.TRANSFER, null -> emptyList()
    }

    private fun changePart(index: Int, transform: (PartDraft) -> PartDraft) = change {
        it.copy(parts = it.parts.mapIndexed { position, part -> if (position == index) transform(part) else part })
    }

    // Updates the state, what the parts leave and whether it can be confirmed.
    private fun change(transform: (EditUiState) -> EditUiState) = state.update { current ->
        transform(current).let { edit ->
            val amount = edit.amount.toLongOrNull() ?: 0
            val remainder = amount - edit.parts.sumOf { it.amount.toLongOrNull() ?: 0 }
            val parts = edit.parts.all { it.categoryId != null && (it.amount.toLongOrNull() ?: 0) > 0 }
            edit.copy(
                remainder = remainder,
                // The edited transaction keeps the rest: the parts never take the whole amount.
                canConfirm = edit.transaction != null && amount > 0 && parts && remainder > 0,
            )
        }
    }

    private companion object {
        const val SUGGESTIONS = 3

        // As many digits as the largest amount.
        const val MAX_DIGITS = 8
    }
}
