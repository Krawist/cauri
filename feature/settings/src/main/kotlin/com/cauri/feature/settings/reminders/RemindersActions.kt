package com.cauri.feature.settings.reminders

import java.time.LocalTime

/** What the user can do on the smart reminders screen. */
internal interface RemindersActions {
    fun onBack()

    fun onVisitReminders(on: Boolean)

    /** Opens or closes the sheet of the time of the evening summary. */
    fun onSummary(open: Boolean)

    /** Has the evening summary come at [time], or follow the user's answers with `null`. */
    fun onSummaryTime(time: LocalTime?)

    /** Opens or closes the sheet of the quiet hours. */
    fun onQuietHours(open: Boolean)

    /** Moves the start of the quiet hours by [hours], forward or back, round the clock. */
    fun onQuietFrom(hours: Int)

    /** Moves the end of the quiet hours by [hours], forward or back, round the clock. */
    fun onQuietUntil(hours: Int)

    /** Changes the daily cap by [step], within its bounds. */
    fun onDailyCap(step: Int)

    /** Opens « Mes lieux », where a place's reminder is changed. */
    fun onPlaces()
}
