package com.cauri.core.data.model

/**
 * An account to create, such as one declared during the onboarding. The Cash account always exists.
 *
 * @property name Name shown to the user (« MTN MoMo », « Afriland First Bank »).
 * @property kind Mobile money or bank.
 * @property provider Stable code of the operator or bank: a [MobileMoney.code] or a [Bank.code].
 * @property currency ISO 4217 code of the account's amounts.
 */
data class NewAccount(val name: String, val kind: AccountKind, val provider: String, val currency: String)
