package com.cauri.core.data.preferences

import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not business data. The Room database holds everything else. */
interface UserPreferencesRepository {
    /** Whether the user went through the onboarding. */
    val onboardingCompleted: Flow<Boolean>

    /** The user's profile; `null` until the onboarding saved it. */
    val profile: Flow<UserProfile?>

    /** The user's choice for the cash count reminders; « Quand c'est utile » from 20 h until changed. */
    val countReminder: Flow<CountReminderSettings>

    /** Day of the last cash count reminder; `null` when none was sent. */
    val lastCountReminder: Flow<LocalDate?>

    suspend fun setOnboardingCompleted()

    /** Saves the whole [profile], replacing the stored one. */
    suspend fun saveProfile(profile: UserProfile)

    suspend fun saveCountReminder(settings: CountReminderSettings)

    /** Remembers that a cash count reminder was sent on [day]. */
    suspend fun setLastCountReminder(day: LocalDate)
}
