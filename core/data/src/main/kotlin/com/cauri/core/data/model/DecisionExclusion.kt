package com.cauri.core.data.model

/** Why a decision is left out of the training of the bandit: its outcome says nothing of the reminder. */
enum class DecisionExclusion {
    /** Set aside within two seconds of the notification: a tap by mistake. */
    ACCIDENTAL,

    /** A notification decided that Android never showed: the notifications of Cauri are off. */
    UNSEEN,
}
