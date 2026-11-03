package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountBalanceDao
import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Default implementation for [AccountRepository]. */
@Singleton
internal class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val balanceDao: AccountBalanceDao,
) : AccountRepository {
    override val accounts: Flow<List<Account>> = accountDao.observeActive().map { accounts ->
        accounts.map { it.toModel() }
    }

    override val balances: Flow<List<AccountBalance>> =
        combine(accountDao.observeActive(), balanceDao.observeLatest()) { accounts, balances ->
            val latest = balances.associateBy { it.accountId }
            accounts
                .filter { it.kind != AccountKind.CASH }
                .sortedBy { it.kind.ordinal }
                .map { account ->
                    val balance = latest[account.id]
                    AccountBalance(account = account.toModel(), amount = balance?.amount, at = balance?.at)
                }
        }

    override suspend fun recordBalance(accountId: Long, amount: Long, at: Instant) = balanceDao.insert(
        AccountBalanceEntity(accountId = accountId, amount = amount, at = at, rawSignalId = null),
    )

    override suspend fun createAccounts(accounts: List<NewAccount>) {
        accountDao.insertAll(accounts.map { it.toEntity() })
    }

    override suspend fun createAccount(account: NewAccount): Long = accountDao.insert(account.toEntity())

    override suspend fun accountOf(provider: String): Account? = accountDao.getByProvider(provider)?.toModel()

    private fun AccountEntity.toModel() =
        Account(id = id, name = name, kind = kind, provider = provider, currency = currency)

    private fun NewAccount.toEntity() = AccountEntity(
        name = name,
        kind = kind,
        provider = provider,
        currency = currency,
        status = AccountStatus.ACTIVE,
    )
}
