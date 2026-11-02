package com.cauri.core.data.repository

import com.cauri.core.data.model.NewAccount

/** In-memory [AccountRepository] for tests. */
class FakeAccountRepository : AccountRepository {
    val created = mutableListOf<NewAccount>()

    override suspend fun createAccounts(accounts: List<NewAccount>) {
        created += accounts
    }
}
