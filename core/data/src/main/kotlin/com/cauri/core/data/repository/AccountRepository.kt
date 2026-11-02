package com.cauri.core.data.repository

import com.cauri.core.data.model.NewAccount

/** The user's accounts; the Cash account always exists. */
interface AccountRepository {
    /** Creates [accounts] as active accounts, all or none. */
    suspend fun createAccounts(accounts: List<NewAccount>)
}
