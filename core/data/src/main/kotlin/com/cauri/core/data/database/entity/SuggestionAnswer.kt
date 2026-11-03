package com.cauri.core.data.database.entity

/** The user's answer to « Tu viens souvent ici. C'est un commerce ? ». */
internal enum class SuggestionAnswer {
    /** « Oui »: the user saved the place. */
    ACCEPTED,

    /** « Non »: the place is ignored for good. */
    REFUSED,
}
