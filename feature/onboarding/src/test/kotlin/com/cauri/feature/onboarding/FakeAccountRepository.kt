package com.cauri.feature.onboarding

import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.repository.AccountRepository

/** In-memory [AccountRepository] for tests. */
internal class FakeAccountRepository : AccountRepository {
    val created = mutableListOf<NewAccount>()

    override suspend fun createAccounts(accounts: List<NewAccount>) {
        created += accounts
    }
}
