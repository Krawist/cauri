package com.cauri.feature.home

import com.cauri.core.data.model.CashBalance

/**
 * What the home screen shows.
 *
 * @property initial First letter of the user's name, in capitals, on the profile button; empty until it is read.
 * @property cash Estimated cash in pocket; `null` until it is read, or before the first count.
 */
internal data class HomeUiState(val initial: String = "", val cash: CashBalance? = null)
