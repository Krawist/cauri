package com.cauri.core.data.preferences

import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(false)
    override val profile = MutableStateFlow<UserProfile?>(null)
    override val countReminder = MutableStateFlow(
        CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
    )
    override val reminders = MutableStateFlow(
        ReminderSettings(quietFrom = 22, quietUntil = 7, dailyCap = 4, visitReminders = true),
    )
    override val incomeSplit = MutableStateFlow(IncomeSplitSettings(savingsRate = 10, threshold = 10_000))
    override val appLock = MutableStateFlow(false)
    override val emergencyFundCreated = MutableStateFlow(false)
    override val seenHelp = MutableStateFlow<Set<String>>(emptySet())
    override val learningSince = MutableStateFlow<Instant?>(null)
    override val lastEveningSummary = MutableStateFlow<LocalDate?>(null)
    override val lastCountReminder = MutableStateFlow<LocalDate?>(null)
    override val lastCaptureWarning = MutableStateFlow<Instant?>(null)
    override val proposedAccounts = MutableStateFlow<Set<String>>(emptySet())
    override val ignoredAccounts = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }

    override suspend fun saveCountReminder(settings: CountReminderSettings) {
        countReminder.value = settings
    }

    override suspend fun saveReminders(settings: ReminderSettings) {
        reminders.value = settings
    }

    override suspend fun saveIncomeSplit(settings: IncomeSplitSettings) {
        incomeSplit.value = settings
    }

    override suspend fun setAppLock(on: Boolean) {
        appLock.value = on
    }

    override suspend fun setEmergencyFundCreated() {
        emergencyFundCreated.value = true
    }

    override suspend fun markHelpSeen(id: String) {
        seenHelp.value += id
    }

    override suspend fun setLearningSince(at: Instant) {
        learningSince.value = at
    }

    override suspend fun setLastEveningSummary(day: LocalDate) {
        lastEveningSummary.value = day
    }

    override suspend fun setLastCountReminder(day: LocalDate) {
        lastCountReminder.value = day
    }

    override suspend fun setLastCaptureWarning(at: Instant) {
        lastCaptureWarning.value = at
    }

    override suspend fun proposeAccount(provider: String) {
        if (provider !in ignoredAccounts.value) proposedAccounts.value += provider
    }

    override suspend fun ignoreAccount(provider: String) {
        proposedAccounts.value -= provider
        ignoredAccounts.value += provider
    }

    override suspend fun clearAccountChoice(provider: String) {
        proposedAccounts.value -= provider
        ignoredAccounts.value -= provider
    }
}
