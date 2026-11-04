package com.cauri.feature.transactions.entry

/**
 * The free text of the quick entry: « taxi 1500, beignets 500 ».
 *
 * @property available Whether the phone's language model can read it; the field is hidden otherwise.
 * @property text Text typed.
 * @property reading Whether the model is reading it.
 * @property lines The expenses read, to save once the user agrees; `null` until read.
 * @property nothingFound Whether the model found no expense with an amount written in the text.
 * @property saved Whether the expenses were saved: the sheet closes.
 */
internal data class FreeTextUiState(
    val available: Boolean = false,
    val text: String = "",
    val reading: Boolean = false,
    val lines: List<FreeTextLine>? = null,
    val nothingFound: Boolean = false,
    val saved: Boolean = false,
)
