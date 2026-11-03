package com.cauri.core.data.model

/** Where an operation of « Activité » stands with the user. */
enum class ActivityStatus {
    /** Read in a message, waiting for the user's confirmation (« À confirmer »). */
    PENDING,

    /** Confirmed, typed by the user, or a fee: counted. */
    CONFIRMED,

    /** Set aside (« Rien dépensé », « Ignorer »): shown, never counted. */
    SET_ASIDE,
}
