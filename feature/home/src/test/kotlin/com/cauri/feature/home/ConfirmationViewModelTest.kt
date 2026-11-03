package com.cauri.feature.home

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeConfirmationRepository
import com.cauri.core.data.repository.FakeTransactionRepository
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
internal class ConfirmationViewModelTest {
    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()
    private val confirmations = FakeConfirmationRepository()
    private lateinit var viewModel: ConfirmationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        categories.suggestions[EntryType.EXPENSE] = listOf(food(), snacks())
        viewModel = ConfirmationViewModel(transactions, categories, confirmations)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Cards {
        @Test
        fun `should pre-fill a card with the category Cauri proposes`() {
            // when
            transactions.pending.value = listOf(payment(amount = 5_000))

            // then
            viewModel.uiState.value.cards.single().let { it.categoryId to it.amount } shouldBe (10L to 5_000L)
        }
    }

    @Nested
    inner class OnConfirm {
        @Test
        fun `should confirm the card with the category chosen, next to the proposal`() {
            // given
            transactions.pending.value = listOf(payment(amount = 5_000))
            viewModel.onCategory(transactionId = 7, categoryId = 11)

            // when
            viewModel.onConfirm(7)

            // then
            confirmations.confirmed shouldContainExactly listOf(
                FakeConfirmationRepository.Confirmed(
                    operation = payment(amount = 5_000),
                    categoryId = 11,
                    proposedCategoryId = 10,
                    amount = 5_000,
                ),
            )
            viewModel.uiState.value.let { it.cards to it.answered } shouldBe
                (emptyList<ConfirmationCard>() to AnsweredCard(transactionId = 7, confirmed = true))
        }

        @Test
        fun `should ask for the amount of a payment in another currency first`() {
            // given
            transactions.pending.value = listOf(payment(amount = 0))

            // when
            viewModel.onConfirm(7)

            // then
            viewModel.uiState.value.editingAmount shouldBe 7L
            confirmations.confirmed.shouldBeEmpty()
        }

        @Test
        fun `should confirm the amount the user typed`() {
            // given
            transactions.pending.value = listOf(payment(amount = 0))
            viewModel.onEditAmount(7)
            viewModel.onAmount(6_500)

            // when
            viewModel.onConfirm(7)

            // then
            confirmations.confirmed.single().amount shouldBe 6_500L
        }
    }

    @Nested
    inner class OnDismiss {
        @Test
        fun `should set the card aside`() {
            // given
            transactions.pending.value = listOf(payment(amount = 5_000))

            // when
            viewModel.onDismiss(7)

            // then
            confirmations.dismissed shouldContainExactly listOf(payment(amount = 5_000))
            viewModel.uiState.value.answered shouldBe AnsweredCard(transactionId = 7, confirmed = false)
        }
    }

    @Nested
    inner class OnUndo {
        @Test
        fun `should put the card back to confirm`() {
            // given
            transactions.pending.value = listOf(payment(amount = 5_000))
            viewModel.onConfirm(7)

            // when
            viewModel.onUndo()

            // then
            confirmations.reopened shouldContainExactly listOf(7L)
        }
    }

    private fun payment(amount: Long) = PendingOperation(
        transactionId = 7,
        accountId = 2,
        type = PendingType.EXPENSE,
        amount = amount,
        foreignAmount = null,
        foreignCurrency = null,
        accountName = "Orange Money",
        accountKind = AccountKind.MOBILE_MONEY,
        toAccountName = null,
        toAccountKind = null,
        counterparty = "BOULANGERIE",
        occurredAt = Instant.ofEpochSecond(1_000),
    )

    private fun food() = Category(id = 10, name = "Alimentation", nature = CategoryNature.EXPENSE)

    private fun snacks() = Category(id = 11, name = "Snacks", nature = CategoryNature.EXPENSE)
}
