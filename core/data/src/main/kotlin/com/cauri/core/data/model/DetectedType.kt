package com.cauri.core.data.model

/** What a message reports, as Cauri records it. */
enum class DetectedType {
    /** Money spent or sent to someone. */
    EXPENSE,

    /** Money received. */
    INCOME,

    /** Cash taken out of the account: a transfer to the Cash account. */
    WITHDRAWAL,

    /** Cash put into the account: a transfer from the Cash account. */
    DEPOSIT,

    /** Money sent to another account of the user's own: [DetectedOperation.counterpartAccountId]. */
    TRANSFER_OUT,

    /** Money received from another account of the user's own: [DetectedOperation.counterpartAccountId]. */
    TRANSFER_IN,
}
