package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.ActivityLine
import java.time.LocalDate

/**
 * The lines of one day of « Activité », or of the whole selection when sorted by amount.
 *
 * @property date Day, in the phone's time zone; `null` for the lines sorted by amount, shown without days.
 * @property lines Lines, newest first, or largest amount first.
 */
internal data class ActivityDay(val date: LocalDate?, val lines: List<ActivityLine>)
