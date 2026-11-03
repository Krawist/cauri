package com.cauri.core.data.model

/**
 * What became of a [ManualEntry].
 *
 * @property transactionId Transaction that now counts for it: the entry itself, or the detected transaction it was
 *   merged into.
 * @property mergeId Merge to offer to undo; `null` when the entry was recorded on its own.
 */
data class RecordedEntry(val transactionId: Long, val mergeId: Long?)
