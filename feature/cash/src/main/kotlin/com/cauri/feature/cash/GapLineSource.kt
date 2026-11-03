package com.cauri.feature.cash

/** Where a line of the gap split comes from. */
internal enum class GapLineSource {
    /** A habit: Cauri noticed it might have been paid. */
    HABIT,

    /** A visit to a shop since the count: Cauri noticed it too. */
    VISIT,

    /** An expense the user added. */
    ADDED,
}
