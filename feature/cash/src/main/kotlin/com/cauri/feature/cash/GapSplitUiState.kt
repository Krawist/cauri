package com.cauri.feature.cash

import com.cauri.core.data.model.Category
import java.time.Instant

/**
 * What the gap split screen shows.
 *
 * @property gap Money missing after the count, in FCFA.
 * @property since When the previous count was made, or the balance before the one typed; `null` until it is read.
 * @property accountName Digital account whose typed balance left the gap; `null` for a cash count.
 * @property lines Moments Cauri noticed, then the expenses the user added.
 * @property categories Spending categories an added expense can go into, by name.
 * @property assigned Sum of the typed amounts, in FCFA.
 * @property rest Part of the gap nobody placed, in FCFA, never below 0: recorded as « Divers cash » for the cash,
 *   nothing for a digital account.
 * @property picking Whether the category list of a new expense is open.
 * @property saved Whether everything was saved, so the screens can close.
 */
internal data class GapSplitUiState(
    val gap: Long = 0,
    val since: Instant? = null,
    val accountName: String? = null,
    val lines: List<GapLine> = emptyList(),
    val categories: List<Category> = emptyList(),
    val assigned: Long = 0,
    val rest: Long = 0,
    val picking: Boolean = false,
    val saved: Boolean = false,
)
