package com.cauri.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Small user settings that are not business data. The Room database (CAU-05) holds everything else. */
@Singleton
class UserPreferencesRepository @Inject internal constructor(private val dataStore: DataStore<Preferences>) {
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[OnboardingCompleted] ?: false }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }
}
