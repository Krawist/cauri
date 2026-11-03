package com.cauri.core.parsing.model

/** What a message reports. Money received is a [TRANSFER] in the [Direction.CREDIT] direction. */
enum class MessageKind {
    /** A payment to a merchant or a bill. */
    PAYMENT,

    /** Money sent to, or received from, someone. */
    TRANSFER,

    /** Cash taken out of the account. */
    WITHDRAWAL,

    /** Cash put into the account. */
    DEPOSIT,
}
