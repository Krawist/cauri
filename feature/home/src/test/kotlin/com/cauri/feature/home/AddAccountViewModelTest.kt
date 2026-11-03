package com.cauri.feature.home

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.repository.FakeAccountRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddAccountViewModelTest {
    private val accounts = FakeAccountRepository()
    private lateinit var viewModel: AddAccountViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddAccountViewModel(accounts)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should list the services the user does not follow, before the banks`() = runTest {
            // given
            viewModel.uiState.launchIn(backgroundScope)
            accounts.accounts.value = listOf(
                Account(
                    id = 2,
                    name = "MTN MoMo",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "MTN_MOMO",
                    currency = "XAF",
                ),
            )

            // when
            val state = viewModel.uiState.first { choices -> choices.choices.none { it.provider == "MTN_MOMO" } }

            // then
            state.choices.first().provider shouldBe "ORANGE_MONEY"
        }
    }

    @Nested
    inner class OnQuery {
        @Test
        fun `should find a bank whatever its accents and case`() = runTest {
            // given
            viewModel.uiState.launchIn(backgroundScope)
            val bank = knownBanks.first { it.name.contains("Afriland") }

            // when
            viewModel.onQuery("AFRILAND")

            // then
            viewModel.uiState.first { it.query == "AFRILAND" }.choices.map { it.provider } shouldContainExactly
                listOf(bank.code)
        }
    }

    @Nested
    inner class OnAdd {
        @Test
        fun `should follow the accounts ticked, then close`() = runTest {
            // given
            viewModel.uiState.launchIn(backgroundScope)
            val bank = knownBanks.first()
            viewModel.onToggle(
                AccountChoice(provider = bank.code, name = bank.name, description = "", kind = AccountKind.BANK),
            )

            // when
            viewModel.onAdd()

            // then
            accounts.created shouldContainExactly listOf(
                NewAccount(name = bank.name, kind = AccountKind.BANK, provider = bank.code, currency = "XAF"),
            )
            viewModel.uiState.first { it.added }.added.shouldBeTrue()
        }
    }
}
