package com.cauri.core.data.model

/** What an operation waiting for the user's confirmation did with the money. */
enum class PendingType {
    EXPENSE,
    INCOME,

    /** Money moved between two of the user's accounts, such as a withdrawal to Cash. */
    TRANSFER,
}
