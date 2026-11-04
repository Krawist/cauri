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
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [UserPreferencesRepository], on DataStore. */
// One function per setting saved.
@Suppress("TooManyFunctions")
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

    override val reminders: Flow<ReminderSettings> = dataStore.data.map {
        ReminderSettings(
            quietFrom = it[QuietFrom] ?: DEFAULT_QUIET_FROM,
            quietUntil = it[QuietUntil] ?: DEFAULT_QUIET_UNTIL,
            dailyCap = it[DailyCap] ?: DEFAULT_DAILY_CAP,
            visitReminders = it[VisitReminders] ?: true,
            summaryTime = it[SummaryMinutes]?.let { minutes ->
                LocalTime.of(minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
            },
        )
    }

    override val incomeSplit: Flow<IncomeSplitSettings> = dataStore.data.map {
        IncomeSplitSettings(
            savingsRate = it[SavingsRate] ?: DEFAULT_SAVINGS_RATE,
            threshold = it[SplitThreshold] ?: DEFAULT_SPLIT_THRESHOLD,
        )
    }

    override suspend fun saveIncomeSplit(settings: IncomeSplitSettings) {
        dataStore.edit {
            it[SavingsRate] = settings.savingsRate
            it[SplitThreshold] = settings.threshold
        }
    }

    override val seenHelp: Flow<Set<String>> = dataStore.data.map { it[SeenHelp].orEmpty() }

    override suspend fun markHelpSeen(id: String) {
        dataStore.edit { it[SeenHelp] = it[SeenHelp].orEmpty() + id }
    }

    override val learningSince: Flow<Instant?> = dataStore.data.map { it[LearningSince]?.let(Instant::ofEpochMilli) }

    override val lastEveningSummary: Flow<LocalDate?> = dataStore.data.map {
        it[LastSummaryDay]?.let(LocalDate::ofEpochDay)
    }

    override val lastCountReminder: Flow<LocalDate?> = dataStore.data.map {
        it[LastReminderDay]?.let(LocalDate::ofEpochDay)
    }

    override val lastCaptureWarning: Flow<Instant?> = dataStore.data.map {
        it[LastCaptureWarning]?.let(Instant::ofEpochMilli)
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

    override suspend fun saveReminders(settings: ReminderSettings) {
        dataStore.edit {
            it[QuietFrom] = settings.quietFrom
            it[QuietUntil] = settings.quietUntil
            it[DailyCap] = settings.dailyCap
            it[VisitReminders] = settings.visitReminders
            val time = settings.summaryTime
            if (time == null) {
                it.remove(SummaryMinutes)
            } else {
                it[SummaryMinutes] = time.hour * MINUTES_PER_HOUR + time.minute
            }
        }
    }

    override suspend fun setLearningSince(at: Instant) {
        dataStore.edit { it[LearningSince] = at.toEpochMilli() }
    }

    override suspend fun setLastEveningSummary(day: LocalDate) {
        dataStore.edit { it[LastSummaryDay] = day.toEpochDay() }
    }

    override suspend fun setLastCountReminder(day: LocalDate) {
        dataStore.edit { it[LastReminderDay] = day.toEpochDay() }
    }

    override suspend fun setLastCaptureWarning(at: Instant) {
        dataStore.edit { it[LastCaptureWarning] = at.toEpochMilli() }
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
        val LastCaptureWarning = longPreferencesKey("capture_last_warning")
        val QuietFrom = intPreferencesKey("reminders_quiet_from")
        val QuietUntil = intPreferencesKey("reminders_quiet_until")
        val DailyCap = intPreferencesKey("reminders_daily_cap")
        val VisitReminders = booleanPreferencesKey("reminders_after_visits")
        val SummaryMinutes = intPreferencesKey("reminders_summary_minutes")
        val LastSummaryDay = longPreferencesKey("reminders_summary_last_day")
        val LearningSince = longPreferencesKey("reminders_learning_since")
        val SeenHelp = stringSetPreferencesKey("help_seen")
        val SavingsRate = intPreferencesKey("income_savings_rate")
        val SplitThreshold = longPreferencesKey("income_split_threshold")
        const val DEFAULT_SAVINGS_RATE = 10
        const val DEFAULT_SPLIT_THRESHOLD = 10_000L
        const val DEFAULT_REMINDER_HOUR = 20
        const val DEFAULT_QUIET_FROM = 22
        const val DEFAULT_QUIET_UNTIL = 7
        const val DEFAULT_DAILY_CAP = 4
        const val MINUTES_PER_HOUR = 60
    }
}
