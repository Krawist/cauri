package com.cauri.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CategoryRepository
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
@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val preferences: UserPreferencesRepository,
    private val cashCounts: CashCountRepository,
    private val categories: CategoryRepository,
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

    /**
     * Saves the profile, the cash in pocket as the first cash count and the chosen [categories], the answers of the
     * last step. The earlier steps always came first: their answers are there.
     */
    fun complete(categories: List<NewCategory>) {
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
            this@OnboardingViewModel.categories.createCategories(categories)
            state.value = true
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
    }
}
