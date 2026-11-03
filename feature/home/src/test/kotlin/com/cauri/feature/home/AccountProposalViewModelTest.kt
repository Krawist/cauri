package com.cauri.feature.home

import com.cauri.core.capture.signal.FakeSignalProcessor
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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
internal class AccountProposalViewModelTest {
    private val processor = FakeSignalProcessor()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Proposal {
        @Test
        fun `should read the unread signals when the home screen opens`() {
            // when
            AccountProposalViewModel(processor)

            // then
            processor.processed shouldBe 1
        }

        @Test
        fun `should offer the first account Cauri recognised messages of`() = runTest {
            // given
            processor.proposedAccounts.value = listOf(orangeMoney(), uba())

            // when
            val viewModel = AccountProposalViewModel(processor)

            // then
            viewModel.proposal.first { it != null } shouldBe orangeMoney()
        }
    }

    @Nested
    inner class OnChoice {
        @Test
        fun `should follow the account when the user accepts`() = runTest {
            // given
            processor.proposedAccounts.value = listOf(orangeMoney())
            val viewModel = AccountProposalViewModel(processor)
            viewModel.proposal.launchIn(backgroundScope)

            // when
            viewModel.onChoice(provider = "ORANGE_MONEY", follow = true)

            // then
            processor.followed shouldContainExactly listOf("ORANGE_MONEY")
            viewModel.proposal.value.shouldBeNull()
        }

        @Test
        fun `should stop offering the account when the user refuses`() = runTest {
            // given
            processor.proposedAccounts.value = listOf(orangeMoney())
            val viewModel = AccountProposalViewModel(processor)
            viewModel.proposal.launchIn(backgroundScope)

            // when
            viewModel.onChoice(provider = "ORANGE_MONEY", follow = false)

            // then
            processor.ignored shouldContainExactly listOf("ORANGE_MONEY")
            viewModel.proposal.value.shouldBeNull()
        }
    }

    private fun orangeMoney() =
        NewAccount(name = "Orange Money", kind = AccountKind.MOBILE_MONEY, provider = "ORANGE_MONEY", currency = "XAF")

    private fun uba() = NewAccount(name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF")
}
