package com.cauri.core.parsing

import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.TemplateCatalog
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.regex.PatternSyntaxException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TemplateCatalogReaderImplTest {
    private val reader = TemplateCatalogReaderImpl()

    @Nested
    inner class Read {
        @Test
        fun `should read a catalogue`() {
            // given
            val json = catalog(
                """{"id": "test.payment", "version": 2, "provider": "TEST_MONEY", "senders": ["TestMoney"], "kind": "PAYMENT",
                "direction": "DEBIT", "currency": "XAF", "pattern": "Paiement de (?<amount>\\d+) F le (?<date>.+)",
                "dateFormat": "dd/MM/yyyy HH:mm"}""",
            )

            // when
            val catalog = reader.read(json)

            // then
            catalog shouldBe TemplateCatalog(
                version = "test",
                templates = listOf(
                    MessageTemplate(
                        id = "test.payment",
                        version = 2,
                        provider = "TEST_MONEY",
                        senders = listOf("TestMoney"),
                        kind = MessageKind.PAYMENT,
                        direction = Direction.DEBIT,
                        currency = "XAF",
                        pattern = "Paiement de (?<amount>\\d+) F le (?<date>.+)",
                        decimalSeparator = null,
                        dateFormat = "dd/MM/yyyy HH:mm",
                        dateLocale = "fr",
                    ),
                ),
            )
        }

        @Test
        fun `should refuse a template without amount`() {
            // given
            val json = catalog(template(pattern = "Paiement de \\\\d+ F"))

            // when & then
            shouldThrow<IllegalArgumentException> { reader.read(json) } shouldHaveMessage
                "Template test.payment has no amount group"
        }

        @Test
        fun `should refuse a date group without date format`() {
            // given
            val json = catalog(template(pattern = "(?<amount>\\\\d+) le (?<date>.+)"))

            // when & then
            shouldThrow<IllegalArgumentException> { reader.read(json) } shouldHaveMessage
                "Template test.payment needs both a date group and a date format, or neither"
        }

        @Test
        fun `should refuse a template without provider`() {
            // given
            val json = catalog(template(provider = "", pattern = "(?<amount>\\\\d+)"))

            // when & then
            shouldThrow<IllegalArgumentException> { reader.read(json) } shouldHaveMessage
                "Template test.payment has no provider"
        }

        @Test
        fun `should refuse a template without sender`() {
            // given
            val json = catalog(template(senders = "", pattern = "(?<amount>\\\\d+)"))

            // when & then
            shouldThrow<IllegalArgumentException> { reader.read(json) } shouldHaveMessage
                "Template test.payment has no sender"
        }

        @Test
        fun `should refuse an identifier used twice`() {
            // given
            val json = catalog(template(pattern = "(?<amount>\\\\d+)"), template(pattern = "(?<amount>\\\\d+) F"))

            // when & then
            shouldThrow<IllegalArgumentException> { reader.read(json) } shouldHaveMessage
                "Template ids used more than once: [test.payment]"
        }

        @Test
        fun `should refuse an invalid pattern`() {
            // given
            val json = catalog(template(pattern = "(?<amount>\\\\d+"))

            // when & then
            shouldThrow<PatternSyntaxException> { reader.read(json) }
        }
    }

    private fun template(provider: String = "TEST_MONEY", senders: String = "\"TestMoney\"", pattern: String) =
        """{"id": "test.payment", "version": 1, "provider": "$provider", "senders": [$senders], "kind": "PAYMENT",
        "direction": "DEBIT", "currency": "XAF", "pattern": "$pattern"}"""

    private fun catalog(vararg templates: String) =
        """{"version": "test", "templates": [${templates.joinToString()}]}"""
}
