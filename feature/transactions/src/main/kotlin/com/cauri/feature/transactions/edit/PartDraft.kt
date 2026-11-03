package com.cauri.feature.transactions.edit

/**
 * A part of the amount being split to another category.
 *
 * @property categoryId Category of the part; `null` until one is chosen.
 * @property amount Digits typed, in FCFA; empty before the first one.
 */
internal data class PartDraft(val categoryId: Long? = null, val amount: String = "")
