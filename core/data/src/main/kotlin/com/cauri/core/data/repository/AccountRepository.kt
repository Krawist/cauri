package com.cauri.core.data.repository

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.NewAccount
import kotlinx.coroutines.flow.Flow

/** The user's accounts; the Cash account always exists. */
interface AccountRepository {
    /** The active accounts, Cash first, again after each change. */
    val accounts: Flow<List<Account>>

    /**
     * The balances of the active accounts other than Cash, as their last message gave them: mobile money first, then
     * the banks and the cards. An account whose messages never gave a balance is left out.
     */
    val balances: Flow<List<AccountBalance>>

    /** Creates [accounts] as active accounts, all or none. */
    suspend fun createAccounts(accounts: List<NewAccount>)

    /** Creates [account] as an active account and returns its id. */
    suspend fun createAccount(account: NewAccount): Long

    /** The account of [provider], hidden or not; `null` when the user has none. */
    suspend fun accountOf(provider: String): Account?
}
