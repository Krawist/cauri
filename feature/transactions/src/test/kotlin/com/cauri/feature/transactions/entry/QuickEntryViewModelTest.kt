package com.cauri.feature.transactions.entry

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CASH_PROVIDER
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.repository.FakeAccountRepository
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.data.repository.FakeTransferRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class QuickEntryViewModelTest {
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val transfers = FakeTransferRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T08:30:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: QuickEntryViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        accounts.accounts.value = listOf(cash(), orangeMoney())
        categories.suggestions[EntryType.EXPENSE] = listOf(transport(), food())
        viewModel = QuickEntryViewModel(accounts, categories, transactions, transfers, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnOpen {
        @Test
        fun `should start on a cash expense with the most likely category chosen`() {
            // when
            viewModel.onOpen()

            // then
            viewModel.uiState.value.let { Triple(it.type, it.accountId, it.categoryId) } shouldBe
                Triple(QuickEntryType.EXPENSE, 1L, 10L)
        }
    }

    @Nested
    inner class OnDigits {
        @Test
        fun `should add the digits typed and never keep a leading zero`() {
            // when
            listOf("0", "1", "5", "000").forEach(viewModel::onDigits)

            // then
            viewModel.uiState.value.amount shouldBe "15000"
        }

        @Test
        fun `should take an amount of 10 000 000 F`() {
            // when
            listOf("1", "0", "000", "000").forEach(viewModel::onDigits)

            // then
            viewModel.uiState.value.let { it.amount to it.amountTooHigh } shouldBe ("10000000" to false)
        }

        @Test
        fun `should refuse a digit that takes the amount over 10 000 000 F, with a message`() {
            // given
            listOf("1", "0", "000", "000").forEach(viewModel::onDigits)

            // when
            viewModel.onDigits("1")

            // then
            viewModel.uiState.value.let { it.amount to it.amountTooHigh } shouldBe ("10000000" to true)
        }

        @Test
        fun `should clear the message once a digit is erased`() {
            // given
            listOf("1", "0", "000", "000", "1").forEach(viewModel::onDigits)

            // when
            viewModel.onDelete()

            // then
            viewModel.uiState.value.amountTooHigh.shouldBeFalse()
        }
    }

    @Nested
    inner class OnSave {
        @Test
        fun `should record a cash expense in the chosen category`() {
            // given
            viewModel.onOpen()
            viewModel.onDigits("500")

            // when
            viewModel.onSave()

            // then
            transactions.entries shouldContainExactly listOf(
                ManualEntry(
                    accountId = 1,
                    type = EntryType.EXPENSE,
                    amount = 500,
                    categoryId = 10,
                    note = null,
                    occurredAt = clock.instant(),
                ),
            )
        }

        @Test
        fun `should record a transfer between the two accounts chosen`() {
            // given
            viewModel.onOpen()
            viewModel.onType(QuickEntryType.TRANSFER)
            viewModel.onToAccount(2)
            viewModel.onDigits("5000")

            // when
            viewModel.onSave()

            // then
            transfers.transfers shouldContainExactly listOf(
                FakeTransferRepository.Transfer(
                    fromAccountId = 1,
                    toAccountId = 2,
                    amount = 5_000,
                    occurredAt = clock.instant(),
                ),
            )
        }

        @Test
        fun `should not record a transfer before its destination is chosen`() {
            // given
            viewModel.onOpen()
            viewModel.onType(QuickEntryType.TRANSFER)
            viewModel.onDigits("5000")

            // when
            viewModel.onSave()

            // then
            viewModel.uiState.value.canSave.shouldBeFalse()
            transfers.transfers.shouldBeEmpty()
        }

        @Test
        fun `should offer to undo when the entry was merged into a detected operation`() {
            // given
            transactions.mergeId = 4
            viewModel.onOpen()
            viewModel.onDigits("2500")

            // when
            viewModel.onSave()

            // then
            viewModel.uiState.value.let { it.saved to it.mergeId } shouldBe (true to 4L)
        }
    }

    @Nested
    inner class OnUndoMerge {
        @Test
        fun `should put the entry and the operation apart again`() {
            // given
            transactions.mergeId = 4
            viewModel.onOpen()
            viewModel.onDigits("2500")
            viewModel.onSave()

            // when
            viewModel.onUndoMerge()

            // then
            viewModel.uiState.value.mergeId.shouldBeNull()
            transactions.undoneMerges shouldContainExactly listOf(4L)
        }
    }

    @Nested
    inner class OnCategory {
        @Test
        fun `should leave the entry without category on a second tap`() {
            // given
            viewModel.onOpen()

            // when
            viewModel.onCategory(10)

            // then
            viewModel.uiState.value.categoryId.shouldBeNull()
        }

        @Test
        fun `should switch to income categories for an income`() {
            // given
            categories.categories.value = listOf(transport(), salary())
            viewModel.onOpen()

            // when
            viewModel.onType(QuickEntryType.INCOME)

            // then
            viewModel.uiState.value.categories shouldContainExactly listOf(salary())
            viewModel.uiState.value.canSave.shouldBeFalse()
            viewModel.onDigits("1")
            viewModel.uiState.value.canSave.shouldBeTrue()
        }
    }

    private fun cash() =
        Account(id = 1, name = "Cash", kind = AccountKind.CASH, provider = CASH_PROVIDER, currency = "XAF")

    private fun orangeMoney() = Account(
        id = 2,
        name = "Orange Money",
        kind = AccountKind.MOBILE_MONEY,
        provider = "ORANGE_MONEY",
        currency = "XAF",
    )

    private fun transport() = Category(id = 10, name = "Transport", nature = CategoryNature.EXPENSE)

    private fun food() = Category(id = 11, name = "Alimentation", nature = CategoryNature.EXPENSE)

    private fun salary() = Category(id = 12, name = "Salaire", nature = CategoryNature.INCOME)
}
