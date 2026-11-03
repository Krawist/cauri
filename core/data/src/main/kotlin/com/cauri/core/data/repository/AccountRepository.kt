package com.cauri.core.data.repository

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.NewAccount

/** The user's accounts; the Cash account always exists. */
interface AccountRepository {
    /** Creates [accounts] as active accounts, all or none. */
    suspend fun createAccounts(accounts: List<NewAccount>)

    /** Creates [account] as an active account and returns its id. */
    suspend fun createAccount(account: NewAccount): Long

    /** The account of [provider], hidden or not; `null` when the user has none. */
    suspend fun accountOf(provider: String): Account?
}
