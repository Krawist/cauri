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

    /** Providers whose messages Cauri recognised without an account to record them on: it offers to follow them. */
    val proposedAccounts: Flow<Set<String>>

    /** Providers the user chose not to follow: their messages stay raw until the user changes their mind. */
    val ignoredAccounts: Flow<Set<String>>

    suspend fun setOnboardingCompleted()

    /** Saves the whole [profile], replacing the stored one. */
    suspend fun saveProfile(profile: UserProfile)

    suspend fun saveCountReminder(settings: CountReminderSettings)

    /** Remembers that a cash count reminder was sent on [day]. */
    suspend fun setLastCountReminder(day: LocalDate)

    /** Offers to follow [provider], unless the user already chose not to. */
    suspend fun proposeAccount(provider: String)

    /** Remembers that the user does not want to follow [provider]. */
    suspend fun ignoreAccount(provider: String)

    /** Forgets the offer or the refusal of [provider], once its account exists. */
    suspend fun clearAccountChoice(provider: String)
}
