package com.cauri.feature.home

/**
 * What « À confirmer » shows.
 *
 * @property cards The operations to confirm, newest first.
 * @property answered Card just answered, while it can be undone; `null` otherwise.
 * @property editingAmount Operation whose amount the user is typing; `null` when the field is closed.
 */
internal data class ConfirmationUiState(
    val cards: List<ConfirmationCard> = emptyList(),
    val answered: AnsweredCard? = null,
    val editingAmount: Long? = null,
)
