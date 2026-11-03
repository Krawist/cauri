package com.cauri.core.data.model

import java.time.Instant

/**
 * The balance of an account, as its last message gave it or the user last typed it, whichever is the latest.
 *
 * @property account Account it is the balance of.
 * @property amount Balance in minor units of the account's currency; negative for an overdrawn bank account; `null`
 *   before a message gave one or the user typed one.
 * @property at When the account had this balance; `null` with [amount].
 */
data class AccountBalance(val account: Account, val amount: Long?, val at: Instant?)
