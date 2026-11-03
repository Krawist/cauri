package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CASH_PROVIDER
import com.cauri.core.data.model.NewAccount
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AccountRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: AccountRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = AccountRepositoryImpl(database.accountDao(), database.accountBalanceDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class CreateAccounts {
        @Test
        fun `should create active accounts next to the Cash account`() = runTest {
            // when
            repository.createAccounts(
                listOf(
                    NewAccount(
                        name = "MTN MoMo",
                        kind = AccountKind.MOBILE_MONEY,
                        provider = "MTN_MOMO",
                        currency = "XAF",
                    ),
                    NewAccount(name = "BICEC", kind = AccountKind.BANK, provider = "BICEC", currency = "XAF"),
                ),
            )

            // then
            database.accountDao().observeActive().first().map { it.copy(id = 0) } shouldContainExactly listOf(
                AccountEntity(
                    name = "Cash",
                    kind = AccountKind.CASH,
                    provider = CASH_PROVIDER,
                    currency = "XAF",
                    status = AccountStatus.ACTIVE,
                ),
                AccountEntity(
                    name = "MTN MoMo",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "MTN_MOMO",
                    currency = "XAF",
                    status = AccountStatus.ACTIVE,
                ),
                AccountEntity(
                    name = "BICEC",
                    kind = AccountKind.BANK,
                    provider = "BICEC",
                    currency = "XAF",
                    status = AccountStatus.ACTIVE,
                ),
            )
        }
    }

    @Nested
    inner class Balances {
        @Test
        fun `should give the latest balance of each account, mobile money before the banks`() = runTest {
            // given
            val ubaId = repository.createAccount(
                NewAccount(name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF"),
            )
            val omId = repository.createAccount(
                NewAccount(
                    name = "Orange Money",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "ORANGE_MONEY",
                    currency = "XAF",
                ),
            )
            val merges = database.transactionMergeDao()
            merges.insertBalance(balance(accountId = ubaId, amount = 215_000, at = 1_000, rawSignalId = signal()))
            merges.insertBalance(balance(accountId = omId, amount = 5_000, at = 1_000, rawSignalId = signal()))
            merges.insertBalance(balance(accountId = omId, amount = 4_500, at = 2_000, rawSignalId = signal()))

            // when
            val balances = repository.balances.first()

            // then
            balances.map { it.account.name to it.amount } shouldContainExactly listOf(
                "Orange Money" to 4_500L,
                "UBA" to 215_000L,
            )
        }

        @Test
        fun `should leave out an account whose messages gave no balance`() = runTest {
            // given
            repository.createAccount(
                NewAccount(name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF"),
            )

            // when
            val balances = repository.balances.first()

            // then
            balances.shouldBeEmpty()
        }
    }

    @Nested
    inner class AccountOf {
        @Test
        fun `should find the account of a provider`() = runTest {
            // given
            val id = repository.createAccount(
                NewAccount(name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF"),
            )

            // when
            val account = repository.accountOf("UBA")

            // then
            account shouldBe Account(id = id, name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF")
        }

        @Test
        fun `should find nothing for a provider without account`() = runTest {
            // when
            val account = repository.accountOf("UBA")

            // then
            account.shouldBeNull()
        }
    }

    private suspend fun signal() = database.rawSignalDao().insert(
        RawSignalEntity(
            sender = "com.samsung.android.messaging",
            title = "OrangeMoney",
            text = "Solde",
            receivedAt = Instant.EPOCH,
            templateVersion = "test@1",
        ),
    )

    private fun balance(accountId: Long, amount: Long, at: Long, rawSignalId: Long) = AccountBalanceEntity(
        accountId = accountId,
        amount = amount,
        at = Instant.ofEpochSecond(at),
        rawSignalId = rawSignalId,
    )
}
