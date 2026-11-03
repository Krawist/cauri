package com.cauri.feature.settings.reminders

/** What Cauri learnt of the user, told in plain words on « Rappels intelligents ». */
internal sealed interface Lesson {
    /**
     * The user answers their cards mostly between two hours.
     *
     * @property from Hour the two busiest hours start, from 0 to 23.
     * @property until Hour they end.
     */
    data class AnswerHours(val from: Int, val until: Int) : Lesson

    /**
     * A visit to a shop leads to an expense most of the time: Cauri asks each time.
     *
     * @property place Name of the shop.
     * @property timesInTen How many visits in ten led to an expense, rounded.
     */
    data class OftenSpends(val place: String, val timesInTen: Int) : Lesson

    /**
     * The user passes by a shop without spending: Cauri no longer asks.
     *
     * @property place Name of the shop.
     */
    data class RarelySpends(val place: String) : Lesson
}
