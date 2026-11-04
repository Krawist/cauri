package com.cauri.core.parsing.freetext

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Default implementation for [FreeTextReader]. */
@Singleton
class FreeTextReaderImpl @Inject constructor() : FreeTextReader {
    private val json = Json { ignoreUnknownKeys = true }

    override fun prompt(text: String, categories: List<String>): String = """
        Extract every expense from the text below, written by someone in Cameroon about cash they spent.
        Answer with a JSON array only, one object per expense: {"label": what was paid, "amount": the amount exactly as
        written in the text, "category": one of [${categories.joinToString()}] or null}.
        Never compute, add or convert an amount. Answer [] when the text holds no expense.
        Text: $text
    """.trimIndent()

    override fun read(answer: String, text: String, categories: List<String>): List<FreeTextExpense> {
        val written = amountsIn(text)
        return items(answer).mapNotNull { item ->
            val amount = item.amount?.filter(Char::isDigit)?.toLongOrNull()
            val label = item.label?.trim().orEmpty()
            if (amount == null || amount !in written || label.isEmpty()) {
                null
            } else {
                FreeTextExpense(
                    label = label,
                    amount = amount,
                    category = categories.firstOrNull {
                        it == item.category
                    },
                )
            }
        }
    }

    // The objects of the JSON array in [answer], whatever the model wrote around it; none when there is no array.
    private fun items(answer: String): List<Item> {
        val start = answer.indexOf('[')
        val end = answer.lastIndexOf(']')
        return if (start < 0 || end < start) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<Item>>(answer.substring(start, end + 1))
            } catch (_: SerializationException) {
                emptyList()
            } catch (_: IllegalArgumentException) {
                emptyList()
            }
        }
    }

    // Every amount written in [text], its digits grouped by spaces or dots or not, up to 10 000 000 F.
    private fun amountsIn(text: String): Set<Long> = AMOUNT.findAll(text)
        .mapNotNull { match -> match.value.filter(Char::isDigit).toLongOrNull() }
        .filter { it in 1..MAX_AMOUNT }
        .toSet()

    // What the model answers for one expense; a field it forgot is `null`.
    @Serializable
    private data class Item(val label: String? = null, val amount: String? = null, val category: String? = null)

    private companion object {
        val AMOUNT = Regex("\\d{1,3}(?:[ .\\u00A0\\u202F]\\d{3})+|\\d+")
        const val MAX_AMOUNT = 10_000_000L
    }
}
