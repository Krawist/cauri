package com.cauri.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeAccountRepository
import com.cauri.core.data.repository.FakeCashCountRepository
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.feature.onboarding.habits.HabitsAnswers
import com.cauri.feature.onboarding.profile.ProfileAnswers
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
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
internal class OnboardingViewModelTest {
    private val savedState = SavedStateHandle()
    private val preferences = FakeUserPreferencesRepository()
    private val cashCounts = FakeCashCountRepository()
    private val categories = FakeCategoryRepository()
    private val accounts = FakeAccountRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-02T08:30:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: OnboardingViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = OnboardingViewModel(savedState, preferences, cashCounts, categories, accounts, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Complete {
        @BeforeEach
        fun answerEarlierSteps() {
            viewModel.onProfileAnswered(
                ProfileAnswers(
                    displayName = "Krawist",
                    occupation = Occupation.TRADER,
                    incomeType = IncomeType.VARIABLE,
                ),
            )
            viewModel.onHabitsAnswered(HabitsAnswers(paymentHabit = PaymentHabit.MOSTLY_CASH, cashAmount = 7_500))
            viewModel.onCategoriesChosen(
                listOf(
                    NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                    NewCategory(name = "Tontine", nature = CategoryNature.SAVING),
                ),
            )
            viewModel.onAccountsChosen(emptyList())
        }

        @Test
        fun `should save the whole profile`() {
            // when
            viewModel.complete()

            // then
            preferences.profile.value shouldBe UserProfile(
                displayName = "Krawist",
                occupation = Occupation.TRADER,
                incomeType = IncomeType.VARIABLE,
                paymentHabit = PaymentHabit.MOSTLY_CASH,
            )
        }

        @Test
        fun `should record the cash in pocket as the first cash count`() {
            // when
            viewModel.complete()

            // then
            cashCounts.counts shouldContainExactly listOf(
                FakeCashCountRepository.Count(amount = 7_500, countedAt = clock.instant(), expectedAmount = null),
            )
        }

        @Test
        fun `should create the categories chosen in the earlier step`() {
            // when
            viewModel.complete()

            // then
            categories.created shouldContainExactly listOf(
                NewCategory(name = "Transport", nature = CategoryNature.EXPENSE),
                NewCategory(name = "Tontine", nature = CategoryNature.SAVING),
            )
        }

        @Test
        fun `should create the accounts declared in the earlier step`() {
            // given
            val declared = listOf(
                NewAccount(name = "MTN MoMo", kind = AccountKind.MOBILE_MONEY, provider = "MTN_MOMO", currency = "XAF"),
                NewAccount(name = "BICEC", kind = AccountKind.BANK, provider = "BICEC", currency = "XAF"),
            )

            viewModel.onAccountsChosen(declared)

            // when
            viewModel.complete()

            // then
            accounts.created shouldContainExactly declared
        }

        @Test
        fun `should end the onboarding once everything is saved`() {
            // when
            viewModel.complete()

            // then
            viewModel.completed.value.shouldBeTrue()
        }

        @Test
        fun `should use the answers kept when the app was killed between two steps`() {
            // given
            val kept = SavedStateHandle(savedState.keys().associateWith { savedState.get<Any>(it) })
            val restored = OnboardingViewModel(kept, preferences, cashCounts, categories, accounts, clock)

            // when
            restored.complete()

            // then
            preferences.profile.value?.displayName shouldBe "Krawist"
        }
    }
}
