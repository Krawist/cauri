package com.cauri.core.data.model

import java.time.LocalDate

/**
 * A goal of the user.
 *
 * @property id Identifier.
 * @property name What the user calls it (« Moto », « Tontine du quartier »).
 * @property kind Saving, tontine or envelope.
 * @property amount In minor units of XAF: the amount to put aside for a saving, the contribution a month for a tontine,
 *   the amount a month for an envelope.
 * @property deadline Day the saving should be reached by; `null` without deadline, and for the other kinds.
 * @property payoutDate Day the user takes the pot of a tontine; `null` when not known, and for the other kinds.
 * @property dayOfMonth Day of the month a tontine's contribution is paid; `null` for the other kinds.
 * @property categoryId Category an envelope keeps money for; `null` for the other kinds.
 */
data class Goal(
    val id: Long,
    val name: String,
    val kind: GoalKind,
    val amount: Long,
    val deadline: LocalDate?,
    val payoutDate: LocalDate?,
    val dayOfMonth: Int?,
    val categoryId: Long?,
)
