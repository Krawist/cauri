package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.NewAccount
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [AccountRepository]. */
@Singleton
internal class AccountRepositoryImpl @Inject constructor(private val accountDao: AccountDao) : AccountRepository {
    override suspend fun createAccounts(accounts: List<NewAccount>) {
        accountDao.insertAll(accounts.map { it.toEntity() })
    }

    override suspend fun createAccount(account: NewAccount): Long = accountDao.insert(account.toEntity())

    override suspend fun accountOf(provider: String): Account? = accountDao.getByProvider(provider)?.let {
        Account(id = it.id, name = it.name, kind = it.kind, provider = it.provider, currency = it.currency)
    }

    private fun NewAccount.toEntity() = AccountEntity(
        name = name,
        kind = kind,
        provider = provider,
        currency = currency,
        status = AccountStatus.ACTIVE,
    )
}
