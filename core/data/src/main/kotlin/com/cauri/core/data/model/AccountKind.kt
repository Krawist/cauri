package com.cauri.core.data.model

/** Kind of an account: where the money lives. */
enum class AccountKind {
    /** Cash in pocket; the Cash account always exists. */
    CASH,

    /** MTN MoMo, Orange Money. */
    MOBILE_MONEY,

    BANK,
}
