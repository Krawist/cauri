package com.cauri.core.data.database.entity

/** How a transaction was created. */
internal enum class TransactionOrigin {
    /** Parsed from a raw signal (an operator's notification). */
    SIGNAL,

    /** Typed by the user. */
    MANUAL,

    /** Extracted from a free-text entry by the on-device model. */
    NATURAL_LANGUAGE,

    /** Created when splitting the gap of a cash count. */
    RECONCILIATION,
}
