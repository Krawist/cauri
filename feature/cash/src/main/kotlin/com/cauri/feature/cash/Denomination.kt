package com.cauri.feature.cash

/**
 * A banknote or coin of the FCFA (XAF) counted on the cash count screen, from the largest.
 *
 * @property value Face value, in FCFA.
 * @property coin Whether it is a coin rather than a banknote.
 */
internal enum class Denomination(val value: Long, val coin: Boolean) {
    NOTE_10000(value = 10_000, coin = false),
    NOTE_5000(value = 5_000, coin = false),
    NOTE_2000(value = 2_000, coin = false),
    NOTE_1000(value = 1_000, coin = false),
    COIN_500(value = 500, coin = true),
    COIN_100(value = 100, coin = true),
    COIN_50(value = 50, coin = true),
    COIN_25(value = 25, coin = true),
}
