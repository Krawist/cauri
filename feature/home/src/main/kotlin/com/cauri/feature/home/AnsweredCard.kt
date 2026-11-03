package com.cauri.feature.home

/**
 * A card just answered, which the user can still undo.
 *
 * @property transactionId Operation answered.
 * @property confirmed Whether it was confirmed; `false` when set aside (« Rien dépensé »).
 */
internal data class AnsweredCard(val transactionId: Long, val confirmed: Boolean)
