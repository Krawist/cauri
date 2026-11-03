package com.cauri.core.data.model

/** The prepaid cards whose messages Cauri reads. Orange names its virtual card « Virtuo » in its SMS. */
val knownPrepaidCards: List<PrepaidCard> = listOf(
    PrepaidCard(code = "ORANGE_MONEY_CARD", name = "Orange Money Prepaid Card", country = "CM"),
)
