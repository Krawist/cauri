package com.cauri.feature.transactions.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Quick entry: an expense, an income or a transfer typed in a few taps, Cash chosen first and the most likely
 * categories suggested. An entry Cauri already detected is merged into it (« Fusionné · Annuler »).
 */
// Injected repositories: one per kind of data the entry reads or writes; one function per gesture of the sheet.
@Suppress("LongParameterList", "TooManyFunctions")
@HiltViewModel
internal class QuickEntryViewModel @Inject constructor(
    accounts: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactions: TransactionRepository,
    private val transfers: TransferRepository,
    private val clock: Clock,
) : ViewModel(),
    QuickEntryActions {
    private val state = MutableStateFlow(QuickEntryUiState())

    val uiState: StateFlow<QuickEntryUiState> = state.asStateFlow()

    // Every active category, by name: the type picks those it shows.
    private var allCategories = emptyList<Category>()

    init {
        viewModelScope.launch {
            combine(accounts.accounts, categoryRepository.categories) { accounts, categories ->
                allCategories = categories
                val first = state.value.accountId == null
                change {
                    it.copy(
                        accounts = accounts,
                        accountId =
                        it.accountId ?: accounts.firstOrNull { account -> account.kind == AccountKind.CASH }?.id,
                        categories = categoriesOf(it.type),
                    )
                }
                if (first) suggest()
            }.collect {}
        }
    }

    override fun onOpen() {
        change {
            QuickEntryUiState(
                accounts = it.accounts,
                accountId = it.accounts.firstOrNull { account -> account.kind == AccountKind.CASH }?.id,
                categories = categoriesOf(QuickEntryType.EXPENSE),
                mergeId = it.mergeId,
            )
        }
        suggest()
    }

    override fun onType(type: QuickEntryType) {
        change {
            it.copy(
                type = type,
                toAccountId = null,
                categories = categoriesOf(type),
                allCategoriesShown = false,
                categoryId = null,
                suggested = emptyList(),
            )
        }
        suggest()
    }

    override fun onAccount(id: Long) {
        change { it.copy(accountId = id, toAccountId = it.toAccountId.takeIf { to -> to != id }) }
        suggest()
    }

    override fun onToAccount(id: Long) = change { it.copy(toAccountId = id) }

    override fun onDigits(digits: String) {
        val amount = (state.value.amount + digits).trimStart('0')
        if ((amount.toLongOrNull() ?: Long.MAX_VALUE) > MAX_AMOUNT) {
            change { it.copy(amountTooHigh = true) }
        } else {
            change { it.copy(amount = amount, amountTooHigh = false) }
        }
    }

    override fun onDelete() = change { it.copy(amount = it.amount.dropLast(1), amountTooHigh = false) }

    override fun onCategory(id: Long) = change { it.copy(categoryId = id.takeIf { chosen -> chosen != it.categoryId }) }

    override fun onShowAllCategories() = change { it.copy(allCategoriesShown = true) }

    override fun onSave() {
        val entry = state.value
        val accountId = entry.accountId
        if (!entry.canSave || accountId == null) return
        change { it.copy(saved = true) }
        viewModelScope.launch {
            val amount = entry.amount.toLong()
            val toAccountId = entry.toAccountId
            if (entry.type == QuickEntryType.TRANSFER && toAccountId != null) {
                transfers.recordTransfer(
                    fromAccountId = accountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    occurredAt = clock.instant(),
                )
            } else {
                val recorded = transactions.recordEntry(
                    ManualEntry(
                        accountId = accountId,
                        type = if (entry.type == QuickEntryType.INCOME) EntryType.INCOME else EntryType.EXPENSE,
                        amount = amount,
                        categoryId = entry.categoryId,
                        note = null,
                        occurredAt = clock.instant(),
                    ),
                )
                recorded.mergeId?.let { mergeId -> change { it.copy(mergeId = mergeId) } }
            }
        }
    }

    override fun onSavedShown() = change { it.copy(saved = false) }

    override fun onUndoMerge() {
        val mergeId = state.value.mergeId ?: return
        change { it.copy(mergeId = null) }
        viewModelScope.launch { transactions.undoMerge(mergeId) }
    }

    override fun onMergeShown() = change { it.copy(mergeId = null) }

    // Suggests the categories of the account and the type, the first chosen: a cash expense then takes three taps.
    private fun suggest() {
        val entry = state.value
        val accountId = entry.accountId ?: return
        val type = when (entry.type) {
            QuickEntryType.EXPENSE -> EntryType.EXPENSE
            QuickEntryType.INCOME -> EntryType.INCOME
            QuickEntryType.TRANSFER -> return
        }
        viewModelScope.launch {
            val suggested = categoryRepository.suggested(accountId = accountId, type = type, limit = SUGGESTIONS)
            change {
                if (it.accountId != accountId || it.type != entry.type) {
                    it
                } else {
                    it.copy(suggested = suggested, categoryId = suggested.firstOrNull()?.id)
                }
            }
        }
    }

    private fun categoriesOf(type: QuickEntryType): List<Category> = when (type) {
        QuickEntryType.EXPENSE -> allCategories.filter { it.nature == CategoryNature.EXPENSE }
        QuickEntryType.INCOME -> allCategories.filter { it.nature == CategoryNature.INCOME }
        QuickEntryType.TRANSFER -> emptyList()
    }

    // Updates the state and whether it can be recorded.
    private fun change(transform: (QuickEntryUiState) -> QuickEntryUiState) = state.update { current ->
        transform(current).let { entry ->
            val amount = entry.amount.toLongOrNull() ?: 0
            val accounts = entry.accountId != null &&
                (entry.type != QuickEntryType.TRANSFER || entry.toAccountId != null)
            entry.copy(canSave = amount > 0 && accounts)
        }
    }

    companion object {
        /** Largest amount an entry takes, in FCFA: far more than any expense, yet never a typing slip. */
        const val MAX_AMOUNT = 10_000_000L

        private const val SUGGESTIONS = 3
    }
}
