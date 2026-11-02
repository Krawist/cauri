package com.cauri.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
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

    // The profile is saved as a whole: it is either complete or absent.
    override val profile: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val firstName = preferences[FirstName]
        val occupation = preferences[OccupationKey]
        val incomeType = preferences[IncomeTypeKey]
        if (firstName != null && occupation != null && incomeType != null) {
            UserProfile(
                displayName = firstName,
                occupation = Occupation.valueOf(occupation),
                incomeType = IncomeType.valueOf(incomeType),
            )
        } else {
            null
        }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit {
            it[FirstName] = profile.displayName
            it[OccupationKey] = profile.occupation.name
            it[IncomeTypeKey] = profile.incomeType.name
        }
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val FirstName = stringPreferencesKey("profile_first_name")
        val OccupationKey = stringPreferencesKey("profile_occupation")
        val IncomeTypeKey = stringPreferencesKey("profile_income_type")
    }
}
