package com.cauri.feature.transactions.edit

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.EditableTransaction
import java.time.Instant

/**
 * State of the edit sheet.
 *
 * @property transaction Transaction edited, as loaded; `null` until it is.
 * @property amount Digits of the whole amount, in FCFA; empty before the first one.
 * @property amountTooHigh Whether the last digit was refused for going over the largest amount.
 * @property accounts The user's active accounts, Cash first.
 * @property accountId Account of the transaction.
 * @property categories Every category of the type of the transaction, by name; empty for a transfer.
 * @property suggested Categories Cauri suggests for the account and the time, the first being its proposal.
 * @property allCategoriesShown Whether « Toutes… » opened every category of the type.
 * @property categoryId Category chosen, of the rest of the amount when split; `null` for none.
 * @property occurredAt When the money moved.
 * @property note Free note typed.
 * @property parts Parts split to other categories, in the order added.
 * @property remainder What the parts leave to [categoryId], in FCFA; below 1 when they take the whole amount.
 * @property canConfirm Whether « Valider » can save: an amount, and every part with its category and amount.
 * @property closed Whether the sheet answered and closes.
 */
internal data class EditUiState(
    val transaction: EditableTransaction? = null,
    val amount: String = "",
    val amountTooHigh: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val accountId: Long? = null,
    val categories: List<Category> = emptyList(),
    val suggested: List<Category> = emptyList(),
    val allCategoriesShown: Boolean = false,
    val categoryId: Long? = null,
    val occurredAt: Instant = Instant.EPOCH,
    val note: String = "",
    val parts: List<PartDraft> = emptyList(),
    val remainder: Long = 0,
    val canConfirm: Boolean = false,
    val closed: Boolean = false,
)
