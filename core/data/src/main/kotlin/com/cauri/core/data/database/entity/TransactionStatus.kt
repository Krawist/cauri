package com.cauri.core.data.database.entity

/** Where a transaction stands with the user. */
internal enum class TransactionStatus {
    /** Created from a signal, waiting for the user's confirmation (« à confirmer »). */
    DETECTED,
    CONFIRMED,

    /** Not a real expense (« Rien dépensé »): kept, but never counted. */
    DISMISSED,

    /** A manual entry merged into the detected transaction of the same operation: kept to undo it, never counted. */
    MERGED,
    ;

    companion object {
        /** The statuses of the transactions left out of every balance. */
        val NOT_COUNTED = listOf(DISMISSED, MERGED)
    }
}
