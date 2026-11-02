package com.cauri.core.data.database.dao

import android.database.SQLException
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.AccountKind
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AccountDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var accountDao: AccountDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        accountDao = database.accountDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetById {
        @Test
        fun `should find the Cash account created with the database`() = runTest {
            // when
            val cash = accountDao.getById(CASH_ACCOUNT_ID)

            // then
            cash shouldBe AccountEntity(
                id = CASH_ACCOUNT_ID,
                name = "Cash",
                kind = AccountKind.CASH,
                provider = null,
                currency = "XAF",
                status = AccountStatus.ACTIVE,
            )
        }

        @Test
        fun `should return null for an unknown account`() = runTest {
            // when
            val account = accountDao.getById(42)

            // then
            account.shouldBeNull()
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should refuse to delete the Cash account`() = runTest {
            // given
            val cash = accountDao.getById(CASH_ACCOUNT_ID).shouldNotBeNull()

            // when
            // Room rethrows the trigger's error as an Android SQLException, whose message the unit test stubs drop.
            shouldThrow<SQLException> { accountDao.delete(cash) }

            // then
            accountDao.getById(CASH_ACCOUNT_ID) shouldBe cash
        }

        @Test
        fun `should delete another account`() = runTest {
            // given
            val momo =
                AccountEntity(
                    name = "MTN MoMo",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "MTN",
                    currency = "XAF",
                    status = AccountStatus.ACTIVE,
                )
            val id = accountDao.insert(momo)

            // when
            accountDao.delete(momo.copy(id = id))

            // then
            accountDao.getById(id).shouldBeNull()
        }
    }

    @Nested
    inner class ObserveActive {
        @Test
        fun `should list the active accounts with Cash first`() = runTest {
            // given
            val momoId = accountDao.insert(
                AccountEntity(
                    name = "MTN MoMo",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "MTN",
                    currency = "XAF",
                    status = AccountStatus.ACTIVE,
                ),
            )
            accountDao.insert(
                AccountEntity(
                    name = "Afriland",
                    kind = AccountKind.BANK,
                    provider = "Afriland",
                    currency = "XAF",
                    status = AccountStatus.HIDDEN,
                ),
            )

            // when
            val active = accountDao.observeActive().first()

            // then
            active.map { it.id } shouldContainExactly listOf(CASH_ACCOUNT_ID, momoId)
        }
    }
}
