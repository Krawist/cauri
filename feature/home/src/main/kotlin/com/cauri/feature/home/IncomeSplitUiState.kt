package com.cauri.feature.home

import com.cauri.core.data.model.IncomeSplitProposal
import com.cauri.core.data.model.PendingIncome

/**
 * The income Cauri offers to split on the home screen.
 *
 * @property income The newest income to split; `null` when there is none.
 * @property proposal The split shown in the sheet, its savings rate as the user moved it; `null` while it is closed.
 */
internal data class IncomeSplitUiState(val income: PendingIncome? = null, val proposal: IncomeSplitProposal? = null)
