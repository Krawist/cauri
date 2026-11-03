package com.cauri.feature.home

import com.cauri.core.data.model.AccountKind

/**
 * An account the user can follow from « Ajouter un compte »: a mobile money service or a bank.
 *
 * @property provider Code of the operator or the bank, the account's provider.
 * @property name Name shown and given to the account.
 * @property description What the bank is known as, empty for most; the row then says « Banque » or « Mobile Money ».
 * @property kind Mobile money or bank.
 */
internal data class AccountChoice(
    val provider: String,
    val name: String,
    val description: String,
    val kind: AccountKind,
)
