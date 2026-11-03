package com.cauri.feature.transactions.activity

/** The accounts « Activité » shows: « Tout », « Cash », « Mobile » or « Banque ». */
internal enum class ActivityFilter {
    ALL,
    CASH,

    /** Mobile money and its prepaid cards. */
    MOBILE,
    BANK,
}
