package com.cauri.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.NewHabit
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.HabitRepository
import com.cauri.feature.onboarding.habits.HabitsAnswers
import com.cauri.feature.onboarding.profile.ProfileAnswers
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The onboarding as a whole, scoped to its navigation graph: it collects the answers of each step and saves them all
 * at the end, so that nothing is saved half-way. The answers survive the app being killed between two steps.
 */
// Injected repositories: one per kind of answer it saves.
@Suppress("LongParameterList")
@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val preferences: UserPreferencesRepository,
    private val cashCounts: CashCountRepository,
    private val categories: CategoryRepository,
    private val accounts: AccountRepository,
    private val habits: HabitRepository,
    private val clock: Clock,
) : ViewModel() {
    private val state = MutableStateFlow(false)

    /** Whether every answer was saved, so the onboarding can end. */
    val completed: StateFlow<Boolean> = state.asStateFlow()

    fun onProfileAnswered(answers: ProfileAnswers) {
        savedState[DISPLAY_NAME] = answers.displayName
        savedState[OCCUPATION] = answers.occupation.name
        savedState[INCOME_TYPE] = answers.incomeType.name
    }

    fun onHabitsAnswered(answers: HabitsAnswers) {
        savedState[PAYMENT_HABIT] = answers.paymentHabit.name
        savedState[CASH_AMOUNT] = answers.cashAmount
    }

    fun onCategoriesChosen(categories: List<NewCategory>, habits: List<FirstHabit>) {
        savedState[CATEGORY_NAMES] = ArrayList(categories.map { it.name })
        savedState[CATEGORY_NATURES] = ArrayList(categories.map { it.nature.name })
        savedState[HABIT_NAMES] = ArrayList(habits.map { it.name })
        savedState[HABIT_AMOUNTS] = habits.map { it.amount }.toLongArray()
        savedState[HABIT_CATEGORY_NAMES] = ArrayList(habits.map { it.categoryName })
    }

    fun onAccountsChosen(accounts: List<NewAccount>) {
        savedState[ACCOUNT_NAMES] = ArrayList(accounts.map { it.name })
        savedState[ACCOUNT_KINDS] = ArrayList(accounts.map { it.kind.name })
        savedState[ACCOUNT_PROVIDERS] = ArrayList(accounts.map { it.provider })
        savedState[ACCOUNT_CURRENCIES] = ArrayList(accounts.map { it.currency })
    }

    /**
     * Saves the profile, the cash in pocket as the first cash count, the categories with their first habits and the
     * accounts, once the permissions, the last step, are given or put off. The earlier steps always came first: their
     * answers are there.
     */
    fun complete() {
        val profile = UserProfile(
            displayName = stored(DISPLAY_NAME),
            occupation = Occupation.valueOf(stored(OCCUPATION)),
            incomeType = IncomeType.valueOf(stored(INCOME_TYPE)),
            paymentHabit = PaymentHabit.valueOf(stored(PAYMENT_HABIT)),
        )
        val cashAmount: Long = stored(CASH_AMOUNT)
        viewModelScope.launch {
            preferences.saveProfile(profile)
            cashCounts.addCount(amount = cashAmount, countedAt = clock.instant(), expectedAmount = null)
            val chosen = chosenCategories()
            val categoryIds = chosen.map { it.name }.zip(categories.createCategories(chosen)).toMap()
            habits.createHabits(
                firstHabits().map {
                    NewHabit(name = it.name, amount = it.amount, categoryId = categoryIds[it.categoryName])
                },
            )
            accounts.createAccounts(chosenAccounts())
            state.value = true
        }
    }

    private fun chosenCategories(): List<NewCategory> {
        val names: List<String> = stored(CATEGORY_NAMES)
        val natures: List<String> = stored(CATEGORY_NATURES)
        return names.zip(natures) { name, nature -> NewCategory(name = name, nature = CategoryNature.valueOf(nature)) }
    }

    private fun firstHabits(): List<FirstHabit> {
        val names: List<String> = stored(HABIT_NAMES)
        val amounts: LongArray = stored(HABIT_AMOUNTS)
        val categoryNames: List<String> = stored(HABIT_CATEGORY_NAMES)
        return names.indices.map {
            FirstHabit(name = names[it], amount = amounts[it], categoryName = categoryNames[it])
        }
    }

    private fun chosenAccounts(): List<NewAccount> {
        val names: List<String> = stored(ACCOUNT_NAMES)
        val kinds: List<String> = stored(ACCOUNT_KINDS)
        val providers: List<String> = stored(ACCOUNT_PROVIDERS)
        val currencies: List<String> = stored(ACCOUNT_CURRENCIES)
        return names.indices.map { index ->
            NewAccount(
                name = names[index],
                kind = AccountKind.valueOf(kinds[index]),
                provider = providers[index],
                currency = currencies[index],
            )
        }
    }

    private fun <T : Any> stored(key: String): T =
        checkNotNull(savedState.get<T>(key)) { "The onboarding step that answers $key comes before the last one" }

    private companion object {
        const val DISPLAY_NAME = "display_name"
        const val OCCUPATION = "occupation"
        const val INCOME_TYPE = "income_type"
        const val PAYMENT_HABIT = "payment_habit"
        const val CASH_AMOUNT = "cash_amount"
        const val CATEGORY_NAMES = "category_names"
        const val CATEGORY_NATURES = "category_natures"
        const val HABIT_NAMES = "habit_names"
        const val HABIT_AMOUNTS = "habit_amounts"
        const val HABIT_CATEGORY_NAMES = "habit_category_names"
        const val ACCOUNT_NAMES = "account_names"
        const val ACCOUNT_KINDS = "account_kinds"
        const val ACCOUNT_PROVIDERS = "account_providers"
        const val ACCOUNT_CURRENCIES = "account_currencies"
    }
}
