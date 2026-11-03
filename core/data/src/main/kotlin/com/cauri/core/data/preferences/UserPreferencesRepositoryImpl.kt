package com.cauri.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [UserPreferencesRepository], on DataStore. */
@Singleton
internal class UserPreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {
    override val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[OnboardingCompleted] ?: false }

    override val profile: Flow<UserProfile?> = dataStore.data.map { it.readProfile() }

    override val countReminder: Flow<CountReminderSettings> = dataStore.data.map {
        CountReminderSettings(
            frequency =
            it[ReminderFrequency]?.let(CountReminderFrequency::valueOf) ?: CountReminderFrequency.WHEN_USEFUL,
            hour = it[ReminderHour] ?: DEFAULT_REMINDER_HOUR,
        )
    }

    override val lastCountReminder: Flow<LocalDate?> = dataStore.data.map {
        it[LastReminderDay]?.let(LocalDate::ofEpochDay)
    }

    override val proposedAccounts: Flow<Set<String>> = dataStore.data.map { it[ProposedAccounts].orEmpty() }

    override val ignoredAccounts: Flow<Set<String>> = dataStore.data.map { it[IgnoredAccounts].orEmpty() }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[OnboardingCompleted] = true }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit {
            it[FirstName] = profile.displayName
            it[OccupationKey] = profile.occupation.name
            it[IncomeTypeKey] = profile.incomeType.name
            it[PaymentHabitKey] = profile.paymentHabit.name
        }
    }

    override suspend fun saveCountReminder(settings: CountReminderSettings) {
        dataStore.edit {
            it[ReminderFrequency] = settings.frequency.name
            it[ReminderHour] = settings.hour
        }
    }

    override suspend fun setLastCountReminder(day: LocalDate) {
        dataStore.edit { it[LastReminderDay] = day.toEpochDay() }
    }

    override suspend fun proposeAccount(provider: String) {
        dataStore.edit {
            if (provider !in
                it[IgnoredAccounts].orEmpty()
            ) {
                it[ProposedAccounts] = it[ProposedAccounts].orEmpty() + provider
            }
        }
    }

    override suspend fun ignoreAccount(provider: String) {
        dataStore.edit {
            it[ProposedAccounts] = it[ProposedAccounts].orEmpty() - provider
            it[IgnoredAccounts] = it[IgnoredAccounts].orEmpty() + provider
        }
    }

    override suspend fun clearAccountChoice(provider: String) {
        dataStore.edit {
            it[ProposedAccounts] = it[ProposedAccounts].orEmpty() - provider
            it[IgnoredAccounts] = it[IgnoredAccounts].orEmpty() - provider
        }
    }

    // The profile is saved as a whole: it is either complete or absent.
    private fun Preferences.readProfile(): UserProfile? = if (ProfileKeys.all { it in this }) {
        UserProfile(
            displayName = stored(FirstName),
            occupation = Occupation.valueOf(stored(OccupationKey)),
            incomeType = IncomeType.valueOf(stored(IncomeTypeKey)),
            paymentHabit = PaymentHabit.valueOf(stored(PaymentHabitKey)),
        )
    } else {
        null
    }

    private fun <T> Preferences.stored(key: Preferences.Key<T>): T = checkNotNull(this[key]) {
        "${key.name} is not stored"
    }

    private companion object {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val FirstName = stringPreferencesKey("profile_first_name")
        val OccupationKey = stringPreferencesKey("profile_occupation")
        val IncomeTypeKey = stringPreferencesKey("profile_income_type")
        val PaymentHabitKey = stringPreferencesKey("profile_payment_habit")
        val ProfileKeys = listOf(FirstName, OccupationKey, IncomeTypeKey, PaymentHabitKey)
        val ReminderFrequency = stringPreferencesKey("count_reminder_frequency")
        val ReminderHour = intPreferencesKey("count_reminder_hour")
        val LastReminderDay = longPreferencesKey("count_reminder_last_day")
        val ProposedAccounts = stringSetPreferencesKey("accounts_proposed")
        val IgnoredAccounts = stringSetPreferencesKey("accounts_ignored")
        const val DEFAULT_REMINDER_HOUR = 20
    }
}
