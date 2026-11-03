package com.cauri.feature.transactions.entry

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.Category

/**
 * What the quick entry sheet shows.
 *
 * @property type Expense, income or transfer.
 * @property amount Digits typed on the keypad, in FCFA; empty until the first one.
 * @property amountTooHigh Whether the last digit typed was refused, the amount going over
 *   [QuickEntryViewModel.MAX_AMOUNT]; cleared by the next digit accepted or erased.
 * @property accounts The active accounts, Cash first.
 * @property accountId Account the money left, or arrived on for an income; Cash when the sheet opens, `null` until the
 *   accounts are read.
 * @property toAccountId Account the money arrives on for a transfer; `null` until chosen.
 * @property suggested Categories most likely for the account and the time of day, the first chosen when they load.
 * @property categories Every category of the type, shown after « Autre… ».
 * @property allCategoriesShown Whether « Autre… » opened every category.
 * @property categoryId Category chosen; `null` when none is.
 * @property canSave Whether « Enregistrer » records: an amount above 0, and both accounts of a transfer chosen.
 * @property saved Whether the entry was just recorded: the sheet closes.
 * @property mergeId Merge of the entry into an operation detected before, which the user can undo for a few seconds;
 *   `null` otherwise.
 */
internal data class QuickEntryUiState(
    val type: QuickEntryType = QuickEntryType.EXPENSE,
    val amount: String = "",
    val amountTooHigh: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val suggested: List<Category> = emptyList(),
    val categories: List<Category> = emptyList(),
    val allCategoriesShown: Boolean = false,
    val categoryId: Long? = null,
    val canSave: Boolean = false,
    val saved: Boolean = false,
    val mergeId: Long? = null,
)
