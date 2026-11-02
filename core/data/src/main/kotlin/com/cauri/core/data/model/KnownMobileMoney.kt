package com.cauri.core.data.model

private const val CAMEROON = "CM"

/** The mobile money services the user can declare. */
val knownMobileMoney: List<MobileMoney> = listOf(
    MobileMoney(code = "MTN_MOMO", name = "MTN MoMo", description = "", country = CAMEROON),
    MobileMoney(code = "ORANGE_MONEY", name = "Orange Money", description = "", country = CAMEROON),
)
