package com.cauri.core.data.model

import java.time.Instant

/**
 * An expense or an income typed by the user on any account.
 *
 * @property accountId Account the money left or arrived on.
 * @property type Money spent or received.
 * @property amount Amount in minor units of the account's currency, always positive.
 * @property categoryId Category; `null` when none was chosen.
 * @property note Free note; `null` when there is none.
 * @property occurredAt When the money moved.
 * @property proposedCategoryId Category Cauri proposed for the entry, whatever the user chose; `null` when it proposed
 *   none.
 */
data class ManualEntry(
    val accountId: Long,
    val type: EntryType,
    val amount: Long,
    val categoryId: Long?,
    val note: String?,
    val occurredAt: Instant,
    val proposedCategoryId: Long? = null,
)
