package com.cauri.core.data.service

import java.time.Instant

/**
 * A category the user chose for a confirmed transaction: what the suggestions learn from.
 *
 * @property categoryId Category chosen.
 * @property occurredAt When the transaction happened.
 */
internal data class CategoryUse(val categoryId: Long, val occurredAt: Instant)
