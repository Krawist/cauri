package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import io.kotest.matchers.collections.shouldContainExactly
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
        repository = AccountRepositoryImpl(database.accountDao())
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
                    provider = null,
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
}
