package com.cauri.core.parsing.freetext

/**
 * Reads the expenses of a free text with an on-device language model, which only extracts: what it answers is kept only
 * when its amounts are written in the text.
 */
interface FreeTextReader {
    /** What the model is asked for [text]: each expense, its label, its amount as written, one of [categories]. */
    fun prompt(text: String, categories: List<String>): String

    /**
     * The expenses of the model's [answer] to the prompt of [text]: each with a label, an amount written in [text]
     * (« 1500 », « 1 500 », « 1.500 ») of at most 10 000 000 F, and a category among [categories] or none; the others
     * are left out.
     */
    fun read(answer: String, text: String, categories: List<String>): List<FreeTextExpense>
}
