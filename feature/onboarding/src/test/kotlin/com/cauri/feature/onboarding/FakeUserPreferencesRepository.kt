package com.cauri.feature.onboarding

import com.cauri.core.data.model.UserProfile
import com.cauri.core.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
internal class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(false)
    override val profile = MutableStateFlow<UserProfile?>(null)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }
}
