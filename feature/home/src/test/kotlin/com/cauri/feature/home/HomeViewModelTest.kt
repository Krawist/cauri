package com.cauri.feature.home

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeAccountRepository
import com.cauri.core.data.repository.FakeAvailableRepository
import com.cauri.core.data.repository.FakeCashCountRepository
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeHabitRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
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
internal class HomeViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private val cashCounts = FakeCashCountRepository()
    private val categories = FakeCategoryRepository()
    private val habits = FakeHabitRepository()
    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository()
    private val available = FakeAvailableRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T08:30:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HomeViewModel(preferences, cashCounts, categories, accounts, available, habits, transactions, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should show what is available today`() {
            // when
            available.today.value = 12_500

            // then
            viewModel.uiState.value.available shouldBe 12_500
        }

        @Test
        fun `should show the first letter of the name in capitals`() {
            // when
            preferences.profile.value = UserProfile(
                displayName = "krawist",
                occupation = Occupation.EMPLOYEE,
                incomeType = IncomeType.FIXED_DATE,
                paymentHabit = PaymentHabit.BOTH,
            )

            // then
            viewModel.uiState.value.initial shouldBe "K"
        }

        @Test
        fun `should greet the user by name on today's date`() {
            // when
            preferences.profile.value = UserProfile(
                displayName = "Krawist",
                occupation = Occupation.EMPLOYEE,
                incomeType = IncomeType.FIXED_DATE,
                paymentHabit = PaymentHabit.BOTH,
            )

            // then
            viewModel.uiState.value.let { it.today to it.name } shouldBe (LocalDate.of(2026, 11, 3) to "Krawist")
        }

        @Test
        fun `should follow the balances of the digital accounts`() {
            // given
            val balance = AccountBalance(
                account = Account(
                    id = 2,
                    name = "Orange Money",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "ORANGE_MONEY",
                    currency = "XAF",
                ),
                amount = 4_500,
                at = Instant.ofEpochSecond(1_000),
            )

            // when
            accounts.balances.value = listOf(balance)

            // then
            viewModel.uiState.value.balances shouldContainExactly listOf(balance)
        }

        @Test
        fun `should follow the estimated cash in pocket`() {
            // given
            val balance = CashBalance(amount = 7_000, countedAt = Instant.ofEpochSecond(1_000), expensesSinceCount = 1)

            // when
            cashCounts.balance.value = balance

            // then
            viewModel.uiState.value.cash shouldBe balance
        }

        @Test
        fun `should offer only the spending categories to a habit`() {
            // when
            categories.categories.value = listOf(
                Category(id = 1, name = "Transport", nature = CategoryNature.EXPENSE),
                Category(id = 2, name = "Tontine", nature = CategoryNature.SAVING),
            )

            // then
            viewModel.uiState.value.categories.map { it.name } shouldContainExactly listOf("Transport")
        }
    }

    @Nested
    inner class OnHabitTap {
        @Test
        fun `should record a cash expense now, in the habit's category`() {
            // when
            viewModel.onHabitTap(Habit(id = 1, name = "Moto", amount = 500, categoryId = 4))

            // then
            transactions.cashExpenses shouldContainExactly listOf(
                FakeTransactionRepository.CashExpense(
                    id = 1,
                    amount = 500,
                    categoryId = 4,
                    note = "Moto",
                    occurredAt = clock.instant(),
                ),
            )
        }

        @Test
        fun `should let the user undo the expense`() {
            // when
            viewModel.onHabitTap(Habit(id = 1, name = "Moto", amount = 500, categoryId = 4))

            // then
            viewModel.uiState.value.recorded shouldBe RecordedHabit(transactionId = 1, name = "Moto", amount = 500)
        }
    }

    @Nested
    inner class OnUndo {
        @Test
        fun `should delete the expense just recorded`() {
            // given
            viewModel.onHabitTap(Habit(id = 1, name = "Moto", amount = 500, categoryId = 4))

            // when
            viewModel.onUndo()

            // then
            transactions.cashExpenses.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnDraftSave {
        @Test
        fun `should point out a missing name and amount`() {
            // given
            viewModel.onEditHabit(null)

            // when
            viewModel.onDraftSave()

            // then
            viewModel.uiState.value.editing shouldBe HabitDraft(nameMissing = true, amountMissing = true)
        }

        @Test
        fun `should add a new habit at the end of the row`() {
            // given
            viewModel.onEditHabit(null)
            viewModel.onDraftChange(HabitDraft(name = " Taxi ", amount = "350", categoryId = 2))

            // when
            viewModel.onDraftSave()

            // then
            habits.habits.value shouldContainExactly listOf(Habit(id = 1, name = "Taxi", amount = 350, categoryId = 2))
        }

        @Test
        fun `should change the edited habit and close the sheet`() {
            // given
            habits.habits.value = listOf(Habit(id = 1, name = "Moto", amount = 500, categoryId = null))
            viewModel.onEditHabit(habits.habits.value.first())
            viewModel.onDraftChange(HabitDraft(id = 1, name = "Moto", amount = "600"))

            // when
            viewModel.onDraftSave()

            // then
            habits.habits.value shouldContainExactly
                listOf(Habit(id = 1, name = "Moto", amount = 600, categoryId = null))
            viewModel.uiState.value.editing.shouldBeNull()
        }
    }

    @Nested
    inner class OnDraftChange {
        @Test
        fun `should keep only the digits of the amount`() {
            // given
            viewModel.onEditHabit(null)

            // when
            viewModel.onDraftChange(HabitDraft(amount = "1 5a0"))

            // then
            viewModel.uiState.value.editing?.amount shouldBe "150"
        }
    }

    @Nested
    inner class OnDraftDelete {
        @Test
        fun `should remove the edited habit`() {
            // given
            habits.habits.value = listOf(Habit(id = 1, name = "Moto", amount = 500, categoryId = null))
            viewModel.onEditHabit(habits.habits.value.first())

            // when
            viewModel.onDraftDelete()

            // then
            habits.habits.value.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnReorder {
        @Test
        fun `should put the habits in the new order`() {
            // given
            habits.habits.value = listOf(
                Habit(id = 1, name = "Moto", amount = 500, categoryId = null),
                Habit(id = 2, name = "Beignets", amount = 300, categoryId = null),
            )

            // when
            viewModel.onReorder(listOf(2, 1))

            // then
            habits.habits.value.map { it.name } shouldContainExactly listOf("Beignets", "Moto")
        }
    }
}
