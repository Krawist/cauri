package com.cauri.core.data.repository

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.NewAccount
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [AccountRepository] for tests: created accounts get the ids 2, 3, 4… in order, Cash being 1. */
class FakeAccountRepository : AccountRepository {
    val created = mutableListOf<NewAccount>()

    /** The accounts [accountOf] finds, by provider. */
    val byProvider = mutableMapOf<String, Account>()

    override val accounts = MutableStateFlow(emptyList<Account>())

    override val balances = MutableStateFlow(emptyList<AccountBalance>())

    override suspend fun createAccounts(accounts: List<NewAccount>) {
        accounts.forEach { createAccount(it) }
    }

    override suspend fun createAccount(account: NewAccount): Long {
        created += account
        val id = created.size + 1L
        byProvider[account.provider] = Account(
            id = id,
            name = account.name,
            kind = account.kind,
            provider = account.provider,
            currency = account.currency,
        )
        return id
    }

    override suspend fun accountOf(provider: String): Account? = byProvider[provider]
}
