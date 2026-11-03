package com.cauri.feature.home

import java.time.LocalDate

/**
 * What « Ce soir » shows above its cards.
 *
 * @property day The day summed up.
 * @property spent Spent today, cash and digital, in FCFA: the waiting operations counted, what was set aside left out.
 * @property cash The part of [spent] paid in cash, in FCFA.
 */
internal data class EveningSummaryUiState(val day: LocalDate, val spent: Long = 0, val cash: Long = 0)
