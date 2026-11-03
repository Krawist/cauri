package com.cauri.feature.settings.reminders

import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.engine.SummaryTime
import java.time.LocalTime

/**
 * What the smart reminders screen shows.
 *
 * @property settings The user's limits on the reminders.
 * @property quietPlaces Names of the places that never bring a reminder: home, work and the shops without one.
 * @property choosingQuietHours Whether the sheet of the quiet hours is open.
 * @property summaryTime When the evening summary comes: the time chosen, or the one that follows the user's answers.
 * @property choosingSummary Whether the sheet of the time of the evening summary is open.
 * @property lessons What Cauri learnt of the user, in plain words.
 * @property answers Answers Cauri learnt from, since it last started from zero.
 * @property weeks Weeks those answers span; 0 within the first week.
 * @property confirmingReset Whether the user is asked to confirm starting again from zero.
 */
internal data class RemindersUiState(
    val settings: ReminderSettings = ReminderSettings(
        quietFrom = DEFAULT_QUIET_FROM,
        quietUntil = DEFAULT_QUIET_UNTIL,
        dailyCap = DEFAULT_DAILY_CAP,
        visitReminders = true,
    ),
    val quietPlaces: List<String> = emptyList(),
    val choosingQuietHours: Boolean = false,
    val summaryTime: LocalTime = SummaryTime.DEFAULT,
    val choosingSummary: Boolean = false,
    val lessons: List<Lesson> = emptyList(),
    val answers: Int = 0,
    val weeks: Int = 0,
    val confirmingReset: Boolean = false,
)

// Until the stored choice is read; the same defaults as the preferences.
private const val DEFAULT_QUIET_FROM = 22
private const val DEFAULT_QUIET_UNTIL = 7
private const val DEFAULT_DAILY_CAP = 4
