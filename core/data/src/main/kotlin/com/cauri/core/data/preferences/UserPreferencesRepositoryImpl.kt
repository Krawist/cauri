package com.cauri.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [UserPreferencesRepository], on DataStore. */
@Singleton
internal class UserPreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {
    override val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[OnboardingCompleted] ?: false }

    override val profile: Flow<UserProfile?> = dataStore.data.map { it.readProfile() }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit {
            it[FirstName] = profile.displayName
            it[OccupationKey] = profile.occupation.name
            it[IncomeTypeKey] = profile.incomeType.name
            it[PaymentHabitKey] = profile.paymentHabit.name
        }
    }

    // The profile is saved as a whole: it is either complete or absent.
    private fun Preferences.readProfile(): UserProfile? = if (ProfileKeys.all { it in this }) {
        UserProfile(
            displayName = stored(FirstName),
            occupation = Occupation.valueOf(stored(OccupationKey)),
            incomeType = IncomeType.valueOf(stored(IncomeTypeKey)),
            paymentHabit = PaymentHabit.valueOf(stored(PaymentHabitKey)),
        )
    } else {
        null
    }

    private fun <T> Preferences.stored(key: Preferences.Key<T>): T = checkNotNull(this[key]) {
        "${key.name} is not stored"
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val FirstName = stringPreferencesKey("profile_first_name")
        val OccupationKey = stringPreferencesKey("profile_occupation")
        val IncomeTypeKey = stringPreferencesKey("profile_income_type")
        val PaymentHabitKey = stringPreferencesKey("profile_payment_habit")
        val ProfileKeys = listOf(FirstName, OccupationKey, IncomeTypeKey, PaymentHabitKey)
    }
}
