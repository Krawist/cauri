package com.cauri.core.data.model

import java.time.Instant

/**
 * The balance of an account, as the operator's last message gave it.
 *
 * @property account Account it is the balance of.
 * @property amount Balance in minor units of the account's currency; negative for an overdrawn bank account.
 * @property at When the account had this balance.
 */
data class AccountBalance(val account: Account, val amount: Long, val at: Instant)
