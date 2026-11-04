package com.cauri.feature.goals

import com.cauri.core.data.model.GoalKind
import java.time.LocalDate

/**
 * A goal being added or edited in its sheet.
 *
 * @property id Goal being edited; `null` for a new one.
 * @property kind Saving, tontine or envelope; chosen for a new goal only.
 * @property name Name as typed.
 * @property amount Amount as typed, digits only, in FCFA: to put aside, the contribution a month, or the amount a month
 *   of an envelope; empty when not given yet.
 * @property deadline Day a saving should be reached by; `null` without deadline.
 * @property payoutDate Day the user takes the pot of a tontine; `null` when not known.
 * @property dayOfMonth Day of the month a tontine's contribution is paid, from 1 to 31.
 * @property categoryId Category of an envelope; `null` until chosen.
 * @property saved What a saving holds, in FCFA; 0 for a new one.
 * @property moving Money typed to put aside or take back, digits only, in FCFA; empty when none.
 * @property first Whether the goal is first: the savings go to the first saving not reached.
 * @property nameMissing Whether the user tried to save without a name.
 * @property amountMissing Whether the user tried to save without an amount above 0.
 * @property categoryMissing Whether the user tried to save an envelope without category.
 */
internal data class GoalDraft(
    val id: Long? = null,
    val kind: GoalKind = GoalKind.SAVING,
    val name: String = "",
    val amount: String = "",
    val deadline: LocalDate? = null,
    val payoutDate: LocalDate? = null,
    val dayOfMonth: Int = 1,
    val categoryId: Long? = null,
    val saved: Long = 0,
    val moving: String = "",
    val first: Boolean = false,
    val nameMissing: Boolean = false,
    val amountMissing: Boolean = false,
    val categoryMissing: Boolean = false,
)
