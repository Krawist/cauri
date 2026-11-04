package com.cauri.core.data.service

/** The rule of the balances read in the messages. */
internal interface BalanceService {
    /**
     * The balance of an account after an operation that moved [change] (positive when money arrived), its message
     * giving [read]: the one read, unless it is still the [previous] balance known while the operation moved money, as
     * some banks forget to update it (UBA); the balance is then the previous one plus [change]. Amounts in minor units.
     */
    fun afterOperation(read: Long, previous: Long?, change: Long): Long
}
