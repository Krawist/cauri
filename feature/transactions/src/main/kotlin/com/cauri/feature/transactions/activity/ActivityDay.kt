package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.ActivityLine
import java.time.LocalDate

/**
 * The lines of one day of « Activité ».
 *
 * @property date Day, in the phone's time zone.
 * @property lines Lines of the day, newest first.
 */
internal data class ActivityDay(val date: LocalDate, val lines: List<ActivityLine>)
