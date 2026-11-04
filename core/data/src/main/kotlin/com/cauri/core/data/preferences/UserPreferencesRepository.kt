package com.cauri.core.data.preferences

import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Small user settings that are not business data. The Room database holds everything else. */
// One function per setting saved.
@Suppress("TooManyFunctions")
interface UserPreferencesRepository {
    /** Whether the user went through the onboarding. */
    val onboardingCompleted: Flow<Boolean>

    /** The user's profile; `null` until the onboarding saved it. */
    val profile: Flow<UserProfile?>

    /** The user's choice for the cash count reminders; « Quand c'est utile » from 20 h until changed. */
    val countReminder: Flow<CountReminderSettings>

    /** The user's limits on the smart reminders; quiet from 22 h to 7 h, 4 a day and after visits until changed. */
    val reminders: Flow<ReminderSettings>

    /** How the user wants their incomes split; 10 % put aside from 10 000 F until changed. */
    val incomeSplit: Flow<IncomeSplitSettings>

    suspend fun saveIncomeSplit(settings: IncomeSplitSettings)

    /** Whether the emergency fund was created once: deleted, it is not created again. */
    val emergencyFundCreated: Flow<Boolean>

    suspend fun setEmergencyFundCreated()

    /** The help bubbles the user closed, by identifier: they never show again. */
    val seenHelp: Flow<Set<String>>

    suspend fun markHelpSeen(id: String)

    /** From when the reminders learn, after the user chose to start again from zero; `null` for the whole log. */
    val learningSince: Flow<Instant?>

    /** Day of the last evening summary; `null` when none was sent. */
    val lastEveningSummary: Flow<LocalDate?>

    /** Day of the last cash count reminder; `null` when none was sent. */
    val lastCountReminder: Flow<LocalDate?>

    /** When the user was last warned that Cauri receives no more messages; `null` when never. */
    val lastCaptureWarning: Flow<Instant?>

    /** Providers whose messages Cauri recognised without an account to record them on: it offers to follow them. */
    val proposedAccounts: Flow<Set<String>>

    /** Providers the user chose not to follow: their messages stay raw until the user changes their mind. */
    val ignoredAccounts: Flow<Set<String>>

    suspend fun setOnboardingCompleted()

    /** Saves the whole [profile], replacing the stored one. */
    suspend fun saveProfile(profile: UserProfile)

    suspend fun saveCountReminder(settings: CountReminderSettings)

    /** Remembers that a cash count reminder was sent on [day]. */
    suspend fun saveReminders(settings: ReminderSettings)

    suspend fun setLearningSince(at: Instant)

    suspend fun setLastEveningSummary(day: LocalDate)

    suspend fun setLastCountReminder(day: LocalDate)

    /** Remembers that the user was warned at [at] that Cauri receives no more messages. */
    suspend fun setLastCaptureWarning(at: Instant)

    /** Offers to follow [provider], unless the user already chose not to. */
    suspend fun proposeAccount(provider: String)

    /** Remembers that the user does not want to follow [provider]. */
    suspend fun ignoreAccount(provider: String)

    /** Forgets the offer or the refusal of [provider], once its account exists. */
    suspend fun clearAccountChoice(provider: String)
}
