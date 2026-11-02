package com.cauri.core.data.model

import java.time.Instant

/**
 * Cash taken out of a Mobile Money or bank account: it becomes a transfer to the Cash account, never an expense.
 *
 * @property accountId Account the cash was taken from.
 * @property amount Amount withdrawn, in minor units of the account's currency, fees excluded.
 * @property fee Fee taken by the operator or the bank, in the same unit; `null` when the message gives none.
 * @property occurredAt When the cash was taken out.
 * @property rawSignalId Signal the withdrawal was parsed from; `null` when the user entered it.
 */
data class Withdrawal(
    val accountId: Long,
    val amount: Long,
    val fee: Long?,
    val occurredAt: Instant,
    val rawSignalId: Long?,
)
