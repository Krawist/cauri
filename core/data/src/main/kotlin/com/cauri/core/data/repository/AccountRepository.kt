package com.cauri.core.data.repository

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.NewAccount
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The user's accounts; the Cash account always exists. */
interface AccountRepository {
    /** The active accounts, Cash first, again after each change. */
    val accounts: Flow<List<Account>>

    /**
     * The balances of the active accounts other than Cash, the latest read in a message or typed by the user: mobile
     * money first, then the banks and the cards. An account without balance yet is listed without amount.
     */
    val balances: Flow<List<AccountBalance>>

    /**
     * Records [amount], in minor units, as the balance the user typed for [accountId] at [at]: the account's balance
     * until a newer one, typed or read in a message, replaces it.
     */
    suspend fun recordBalance(accountId: Long, amount: Long, at: Instant)

    /** Creates [accounts] as active accounts, all or none. */
    suspend fun createAccounts(accounts: List<NewAccount>)

    /** Creates [account] as an active account and returns its id. */
    suspend fun createAccount(account: NewAccount): Long

    /** The account of [provider], hidden or not; `null` when the user has none. */
    suspend fun accountOf(provider: String): Account?
}
