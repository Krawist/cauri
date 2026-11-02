package com.cauri.core.data.preferences

import com.cauri.core.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not business data. The Room database holds everything else. */
interface UserPreferencesRepository {
    /** Whether the user went through the onboarding. */
    val onboardingCompleted: Flow<Boolean>

    /** The user's profile; `null` until the onboarding saved it. */
    val profile: Flow<UserProfile?>

    suspend fun setOnboardingCompleted()

    /** Saves the whole [profile], replacing the stored one. */
    suspend fun saveProfile(profile: UserProfile)
}
