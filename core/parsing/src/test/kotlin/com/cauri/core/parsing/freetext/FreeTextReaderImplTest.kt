package com.cauri.core.parsing.freetext

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class FreeTextReaderImplTest {
    private val reader = FreeTextReaderImpl()

    @Nested
    inner class Read {
        @Test
        fun `should read each expense with an amount written in the text`() {
            // given
            val answer = """Here: [{"label": "taxi", "amount": "1500", "category": "Transport"},
                {"label": "beignets", "amount": "500", "category": "Snacks"}]"""

            // when
            val expenses = reader.read(answer, text = "taxi 1500, beignets 500", categories = CATEGORIES)

            // then
            expenses shouldContainExactly listOf(
                FreeTextExpense(label = "taxi", amount = 1_500, category = "Transport"),
                FreeTextExpense(label = "beignets", amount = 500, category = null),
            )
        }

        @ParameterizedTest
        @ValueSource(strings = ["loyer 50 000", "loyer 50.000", "loyer 50000"])
        fun `should read an amount however its thousands are grouped`(text: String) {
            // given
            val answer = """[{"label": "loyer", "amount": "50 000", "category": null}]"""

            // when & then
            reader.read(answer, text, CATEGORIES) shouldContainExactly
                listOf(FreeTextExpense(label = "loyer", amount = 50_000, category = null))
        }

        @Test
        fun `should leave out an amount the model computed`() {
            // given
            val answer = """[{"label": "taxi et beignets", "amount": "2000", "category": "Transport"}]"""

            // when & then
            reader.read(answer, text = "taxi 1500, beignets 500", categories = CATEGORIES).shouldBeEmpty()
        }

        @Test
        fun `should read nothing from an answer that is no list`() {
            // when & then
            reader.read("I cannot help with that.", text = "taxi 1500", categories = CATEGORIES).shouldBeEmpty()
        }
    }

    @Nested
    inner class Prompt {
        @Test
        fun `should give the model the user's categories and the text`() {
            // when
            val prompt = reader.prompt(text = "taxi 1500", categories = CATEGORIES)

            // then
            prompt shouldContain "[Alimentation, Transport]"
            prompt shouldContain "Text: taxi 1500"
        }
    }

    private companion object {
        val CATEGORIES = listOf("Alimentation", "Transport")
    }
}
