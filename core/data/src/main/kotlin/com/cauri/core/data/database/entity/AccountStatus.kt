package com.cauri.core.data.database.entity

/** Whether an account is shown to the user. */
internal enum class AccountStatus {
    ACTIVE,

    /** Hidden by the user; its transactions are kept. */
    HIDDEN,
}
