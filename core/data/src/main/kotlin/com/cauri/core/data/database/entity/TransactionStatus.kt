package com.cauri.core.data.database.entity

/** Where a transaction stands with the user. */
internal enum class TransactionStatus {
    /** Created from a signal, waiting for the user's confirmation (« à confirmer »). */
    DETECTED,
    CONFIRMED,

    /** Not a real expense (« Rien dépensé »): kept, but never counted. */
    DISMISSED,
}
