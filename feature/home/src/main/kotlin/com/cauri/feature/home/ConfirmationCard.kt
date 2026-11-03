package com.cauri.feature.home

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.PendingOperation

/**
 * A card to confirm: an operation Cauri read in a message, with its proposal.
 *
 * @property operation Operation read.
 * @property suggested Categories Cauri proposes, the most likely first: its proposal; empty for a transfer.
 * @property categoryId Category chosen, the proposal until the user picks another; `null` for none.
 * @property amount Amount to confirm, in FCFA: the one read, or the one the user typed; 0 for a payment in another
 *   currency until the user types what it cost.
 */
internal data class ConfirmationCard(
    val operation: PendingOperation,
    val suggested: List<Category>,
    val categoryId: Long?,
    val amount: Long,
)
