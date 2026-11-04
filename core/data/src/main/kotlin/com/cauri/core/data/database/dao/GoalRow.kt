package com.cauri.core.data.database.dao

import androidx.room.Embedded
import com.cauri.core.data.database.entity.GoalEntity

/**
 * A goal, with the day its tontine's contribution is paid.
 *
 * @property goal The goal.
 * @property dayOfMonth Day of the month of the fixed cost of a tontine; `null` for the other kinds.
 */
internal data class GoalRow(@Embedded val goal: GoalEntity, val dayOfMonth: Int?)
