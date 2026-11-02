package com.cauri.core.data.database.entity

/** Direction of a transaction; its amount is always positive. */
internal enum class TransactionType {
    EXPENSE,
    INCOME,

    /** The money leaving the source account of a transfer. */
    TRANSFER_OUT,

    /** The money arriving on the destination account of a transfer. */
    TRANSFER_IN,
}
