package com.cauri.feature.transactions.edit

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EditableTransaction
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.SplitPart
import com.cauri.core.data.model.TransactionEdit
import com.cauri.core.data.repository.FakeAccountRepository
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeConfirmationRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.data.repository.FakeTransferRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
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
internal class EditViewModelTest {
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val confirmations = FakeConfirmationRepository()
    private val transfers = FakeTransferRepository()
    private lateinit var viewModel: EditViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        categories.categories.value = listOf(food(), family())
        categories.suggestions[EntryType.EXPENSE] = listOf(food())
        viewModel = EditViewModel(accounts, categories, transactions, confirmations, transfers)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnOpen {
        @Test
        fun `should choose Cauri's proposal for a payment without category`() {
            // given
            transactions.editable[7] = payment()

            // when
            viewModel.onOpen(7)

            // then
            viewModel.uiState.value.let { listOf(it.amount, it.categoryId) } shouldBe listOf("2500", 10L)
        }

        @Test
        fun `should keep what was typed when the sheet opens again on the same transaction`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)
            viewModel.onNote("Pain")

            // when
            viewModel.onOpen(7)

            // then
            viewModel.uiState.value.note shouldBe "Pain"
        }
    }

    @Nested
    inner class OnClose {
        @Test
        fun `should forget what was typed once the sheet closed`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)
            viewModel.onNote("Pain")
            viewModel.onClose()

            // when
            viewModel.onOpen(7)

            // then
            viewModel.uiState.value.note shouldBe ""
        }
    }

    @Nested
    inner class OnAccount {
        @Test
        fun `should keep the account of an operation read in a message`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)

            // when
            viewModel.onAccount(1)

            // then
            viewModel.uiState.value.accountId shouldBe 2L
        }
    }

    @Nested
    inner class OnPartAmount {
        @Test
        fun `should refuse parts that leave nothing to the category chosen`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)
            viewModel.onAddPart()
            viewModel.onPartCategory(index = 0, categoryId = 11)

            // when
            viewModel.onPartAmount(index = 0, digits = "2500")

            // then
            viewModel.uiState.value.canConfirm.shouldBeFalse()
        }
    }

    @Nested
    inner class OnConfirm {
        @Test
        fun `should confirm the payment with its parts and Cauri's proposal`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)
            viewModel.onAddPart()
            viewModel.onPartCategory(index = 0, categoryId = 11)
            viewModel.onPartAmount(index = 0, digits = "1000")
            viewModel.onNote("Pain")

            // when
            viewModel.onConfirm()

            // then
            confirmations.edited shouldContainExactly listOf(
                TransactionEdit(
                    transactionId = 7,
                    amount = 2_500,
                    categoryId = 10,
                    proposedCategoryId = 10,
                    accountId = 2,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    note = "Pain",
                    parts = listOf(SplitPart(categoryId = 11, amount = 1_000)),
                ),
            )
        }

        @Test
        fun `should confirm nothing without an amount`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)
            viewModel.onAmount("")

            // when
            viewModel.onConfirm()

            // then
            confirmations.edited.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnDismiss {
        @Test
        fun `should set the payment aside`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)

            // when
            viewModel.onDismiss()

            // then
            confirmations.dismissed shouldContainExactly listOf(7L)
        }
    }

    @Nested
    inner class OnSeparate {
        @Test
        fun `should undo the merge with the entry typed`() {
            // given
            transactions.editable[7] = payment().copy(mergeId = 3)
            viewModel.onOpen(7)

            // when
            viewModel.onSeparate()

            // then
            transactions.undoneMerges shouldContainExactly listOf(3L)
            viewModel.uiState.value.closed.shouldBeTrue()
        }

        @Test
        fun `should undo the link of two messages into a transfer`() {
            // given
            transactions.editable[7] = payment().copy(type = PendingType.TRANSFER, transferLinkId = 4)
            viewModel.onOpen(7)

            // when
            viewModel.onSeparate()

            // then
            transfers.unlinked shouldContainExactly listOf(4L)
        }

        @Test
        fun `should do nothing for an operation Cauri did not join`() {
            // given
            transactions.editable[7] = payment()
            viewModel.onOpen(7)

            // when
            viewModel.onSeparate()

            // then
            transactions.undoneMerges.shouldBeEmpty()
            viewModel.uiState.value.closed.shouldBeFalse()
        }
    }

    private fun payment() = EditableTransaction(
        transactionId = 7,
        type = PendingType.EXPENSE,
        amount = 2_500,
        foreignAmount = null,
        foreignCurrency = null,
        accountId = 2,
        toAccountId = null,
        categoryId = null,
        parts = emptyList(),
        counterparty = "BOULANGERIE",
        note = null,
        occurredAt = Instant.ofEpochSecond(1_000),
        read = true,
        pending = true,
        mergeId = null,
        transferLinkId = null,
    )

    private fun food() = Category(id = 10, name = "Alimentation", nature = CategoryNature.EXPENSE)

    private fun family() = Category(id = 11, name = "Famille", nature = CategoryNature.EXPENSE)
}
