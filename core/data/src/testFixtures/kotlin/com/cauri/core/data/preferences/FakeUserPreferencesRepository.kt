package com.cauri.core.data.preferences

import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [UserPreferencesRepository] for tests. */
class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(false)
    override val profile = MutableStateFlow<UserProfile?>(null)
    override val countReminder = MutableStateFlow(
        CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
    )
    override val lastCountReminder = MutableStateFlow<LocalDate?>(null)

    override suspend fun setOnboardingCompleted() {
        onboardingCompleted.value = true
    }

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }

    override suspend fun saveCountReminder(settings: CountReminderSettings) {
        countReminder.value = settings
    }

    override suspend fun setLastCountReminder(day: LocalDate) {
        lastCountReminder.value = day
    }
}
