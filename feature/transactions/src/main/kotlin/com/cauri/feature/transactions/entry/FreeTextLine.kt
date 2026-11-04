package com.cauri.feature.transactions.entry

/**
 * An expense read in the free text, before the user saves it.
 *
 * @property label What was paid, as the user wrote it, kept as the note.
 * @property amount Amount in FCFA, written in the text.
 * @property categoryId Category the model chose among the user's; `null` when none.
 * @property categoryName Its name; `null` without category.
 */
internal data class FreeTextLine(val label: String, val amount: Long, val categoryId: Long?, val categoryName: String?)
