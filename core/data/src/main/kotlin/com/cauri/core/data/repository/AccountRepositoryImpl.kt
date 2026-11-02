package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.model.NewAccount
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [AccountRepository]. */
@Singleton
internal class AccountRepositoryImpl @Inject constructor(private val accountDao: AccountDao) : AccountRepository {
    override suspend fun createAccounts(accounts: List<NewAccount>) {
        accountDao.insertAll(
            accounts.map {
                AccountEntity(
                    name = it.name,
                    kind = it.kind,
                    provider = it.provider,
                    currency = it.currency,
                    status = AccountStatus.ACTIVE,
                )
            },
        )
    }
}
