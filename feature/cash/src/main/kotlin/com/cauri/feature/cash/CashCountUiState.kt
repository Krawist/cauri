package com.cauri.feature.cash

/**
 * What the cash count screen shows.
 *
 * @property counts Number of each banknote and coin counted; a missing one counts 0.
 * @property typingTotal Whether the user types the total instead of counting banknotes and coins.
 * @property typedTotal Total as typed, digits only, in FCFA.
 * @property total Total counted, in FCFA: the banknotes and coins, or the typed total.
 * @property expected Estimated cash before the count, in FCFA; `null` before the first count.
 * @property gap Expected minus counted, in FCFA: above 0 when money is missing, below 0 for a surplus; `null` without
 * an expected amount.
 * @property saved Whether the count was saved, so the screen can close.
 * @property splitting Whether the missing money is to be split, on the gap split screen.
 */
internal data class CashCountUiState(
    val counts: Map<Denomination, Int> = emptyMap(),
    val typingTotal: Boolean = false,
    val typedTotal: String = "",
    val total: Long = 0,
    val expected: Long? = null,
    val gap: Long? = null,
    val saved: Boolean = false,
    val splitting: Boolean = false,
)
