package com.cauri.core.data.model

/**
 * An account of the user.
 *
 * @property id Identifier.
 * @property name Name shown to the user (« Orange Money », « UBA »).
 * @property kind Cash, mobile money, bank or prepaid card.
 * @property provider Stable code of the operator, bank or card; [CASH_PROVIDER] for the Cash account.
 * @property currency ISO 4217 code of the account's amounts.
 */
data class Account(val id: Long, val name: String, val kind: AccountKind, val provider: String, val currency: String)
