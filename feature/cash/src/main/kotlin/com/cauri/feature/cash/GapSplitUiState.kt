package com.cauri.feature.cash

import com.cauri.core.data.model.Category
import java.time.Instant

/**
 * What the gap split screen shows.
 *
 * @property gap Money missing after the count, in FCFA.
 * @property since When the previous count was made; `null` until it is read.
 * @property lines Moments Cauri noticed, then the expenses the user added.
 * @property categories Spending categories an added expense can go into, by name.
 * @property assigned Sum of the typed amounts, in FCFA.
 * @property rest Part of the gap nobody placed, recorded as « Divers cash », in FCFA; never below 0.
 * @property picking Whether the category list of a new expense is open.
 * @property saved Whether everything was saved, so the screens can close.
 */
internal data class GapSplitUiState(
    val gap: Long = 0,
    val since: Instant? = null,
    val lines: List<GapLine> = emptyList(),
    val categories: List<Category> = emptyList(),
    val assigned: Long = 0,
    val rest: Long = 0,
    val picking: Boolean = false,
    val saved: Boolean = false,
)
