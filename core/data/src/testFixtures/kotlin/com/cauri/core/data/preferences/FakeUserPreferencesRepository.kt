package com.cauri.core.data.preferences

import com.cauri.core.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(false)
    override val profile = MutableStateFlow<UserProfile?>(null)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }
}
