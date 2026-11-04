package com.cauri.core.parsing

import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ModelMessageReaderImplTest {
    private val reader = ModelMessageReaderImpl()

    @Nested
    inner class Read {
        @Test
        fun `should read the operation the model found, each amount written in the message`() {
            // given
            val answer = """Sure: {"kind": "PAYMENT", "direction": "DEBIT", "amount": "2 500", "fees": "50",
                "balance": "12450.50", "counterparty": "Boulangerie Test"}"""

            // when
            val parsed = reader.read(answer, MESSAGE, CATALOG)

            // then
            parsed shouldBe ParsedMessage(
                templateId = ModelMessageReader.TEMPLATE_ID,
                templateVersion = ModelMessageReader.VERSION,
                provider = "TEST_MONEY",
                kind = MessageKind.PAYMENT,
                direction = Direction.DEBIT,
                amount = 2_500,
                currency = "XAF",
                fees = 50,
                balance = 12_451,
                counterparty = "Boulangerie Test",
                reference = null,
                occurredAt = RECEIVED_AT,
            )
        }

        @Test
        fun `should leave the message unread when an amount is not in it`() {
            // given
            val answer = """{"kind": "PAYMENT", "direction": "DEBIT", "amount": "2550", "fees": null}"""

            // when & then
            reader.read(answer, MESSAGE, CATALOG).shouldBeNull()
        }

        @Test
        fun `should leave the message unread when the model found no operation`() {
            // given
            val answer = """{"kind": "NONE", "direction": null, "amount": null}"""

            // when & then
            reader.read(answer, MESSAGE, CATALOG).shouldBeNull()
        }

        @Test
        fun `should leave unread a message of a sender no template knows`() {
            // given
            val answer = """{"kind": "PAYMENT", "direction": "DEBIT", "amount": "2500"}"""

            // when & then
            reader.read(answer, MESSAGE.copy(title = "Inconnu"), CATALOG).shouldBeNull()
        }

        @Test
        fun `should leave the message unread when the answer is no object`() {
            // when & then
            reader.read("I cannot read this.", MESSAGE, CATALOG).shouldBeNull()
        }
    }

    @Nested
    inner class Prompt {
        @Test
        fun `should give the model the message`() {
            // when & then
            reader.prompt(MESSAGE) shouldContain "Message: ${MESSAGE.text}"
        }
    }

    private companion object {
        val RECEIVED_AT: Instant = Instant.parse("2026-11-03T12:00:00Z")
        val MESSAGE = ReceivedMessage(
            packageName = "test.sms",
            title = "TestMoney",
            text = "Paiement de 2 500 FCFA chez Boulangerie Test. Frais 50 FCFA. Solde 12450.50 FCFA.",
            receivedAt = RECEIVED_AT,
        )
        val CATALOG = TemplateCatalog(
            version = "test",
            templates = listOf(
                MessageTemplate(
                    id = "test.payment",
                    version = 1,
                    provider = "TEST_MONEY",
                    senders = listOf("TestMoney"),
                    kind = MessageKind.PAYMENT,
                    direction = Direction.DEBIT,
                    currency = "XAF",
                    pattern = "Achat de (?<amount>[\\d ]+) FCFA",
                ),
            ),
        )
    }
}
