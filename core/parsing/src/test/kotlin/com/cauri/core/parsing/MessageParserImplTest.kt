package com.cauri.core.parsing

import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZoneId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// Made-up messages and templates: the engine, not the operators' formats (CAU-20), is tested here.
internal class MessageParserImplTest {
    private val parser = MessageParserImpl()

    @Nested
    inner class Parse {
        @Test
        fun `should read every value the template declares`() {
            // given
            val template = template(
                pattern = "Paiement de (?<amount>[\\d ]+) F a (?<counterparty>.+?) le (?<date>\\S+ \\S+)\\. " +
                    "Frais (?<fees>[\\d ]+) F\\. Solde (?<balance>[\\d ]+) F\\. Ref (?<reference>\\w+)",
                dateFormat = "dd/MM/yyyy HH:mm",
            )
            val message = message(
                text = "Paiement de 2 500 F a BOULANGERIE TEST le 03/11/2026 08:30. " +
                    "Frais 50 F. Solde 41 300 F. Ref AB12",
            )

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed shouldBe ParsedMessage(
                templateId = "test.payment",
                templateVersion = 1,
                kind = MessageKind.PAYMENT,
                direction = Direction.DEBIT,
                amount = 2_500,
                currency = "XAF",
                fees = 50,
                balance = 41_300,
                counterparty = "BOULANGERIE TEST",
                reference = "AB12",
                occurredAt = Instant.parse("2026-11-03T07:30:00Z"),
            )
        }

        @Test
        fun `should leave the values the template does not declare empty and date it when it was received`() {
            // given
            val template = template(pattern = "Paiement de (?<amount>[\\d ]+) F")
            val message = message(text = "Paiement de 2 500 F")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed shouldBe ParsedMessage(
                templateId = "test.payment",
                templateVersion = 1,
                kind = MessageKind.PAYMENT,
                direction = Direction.DEBIT,
                amount = 2_500,
                currency = "XAF",
                fees = null,
                balance = null,
                counterparty = null,
                reference = null,
                occurredAt = RECEIVED_AT,
            )
        }

        @Test
        fun `should leave an optional value empty when the message does not give it`() {
            // given
            val template = template(pattern = "Paiement de (?<amount>[\\d ]+) F(?: Frais (?<fees>[\\d ]+) F)?")
            val message = message(text = "Paiement de 2 500 F")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed?.fees.shouldBeNull()
        }

        @Test
        fun `should match whatever the case and the kind of spaces of the message`() {
            // given
            val template = template(pattern = "paiement de (?<amount>[\\d ]+) F")
            val message = message(text = "PAIEMENT de  2 500 F")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed?.amount shouldBe 2_500
        }

        @Test
        fun `should read the message with the first template that matches`() {
            // given
            val withdrawal =
                template(id = "test.withdrawal", kind = MessageKind.WITHDRAWAL, pattern = "Retrait (?<amount>\\d+)")
            val payment = template(id = "test.payment", pattern = "(?<amount>\\d+)")
            val message = message(text = "Retrait 10000")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(withdrawal, payment), zone = DOUALA)

            // then
            parsed?.templateId shouldBe "test.withdrawal"
        }

        @Test
        fun `should recognise an app by its package name`() {
            // given
            val template = template(senders = listOf("test.bank"), pattern = "Debit (?<amount>\\d+)")
            val message = message(packageName = "test.bank", title = "Mon compte", text = "Debit 5000")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed?.amount shouldBe 5_000
        }

        @Test
        fun `should never read a message with the template of another sender`() {
            // given
            val template = template(pattern = "Paiement de (?<amount>[\\d ]+) F")
            val message = message(title = "Maman", text = "Paiement de 2 500 F")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed.shouldBeNull()
        }

        @Test
        fun `should not recognise a message that no template matches`() {
            // given
            val template = template(pattern = "Paiement de (?<amount>[\\d ]+) F")
            val message = message(text = "Votre code est 1234")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed.shouldBeNull()
        }

        @Test
        fun `should not recognise a message whose date cannot be read`() {
            // given
            val template =
                template(pattern = "Paiement de (?<amount>\\d+) F le (?<date>\\S+)", dateFormat = "dd/MM/yyyy HH:mm")
            val message = message(text = "Paiement de 2500 F le 03/11/2026")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed.shouldBeNull()
        }

        @Test
        fun `should read the names of the months in the language of the template`() {
            // given
            val template = template(
                pattern = "Debit (?<amount>\\d+) on (?<date>.+)",
                dateFormat = "dd-MMM-yyyy HH:mm",
                dateLocale = "en",
            )
            val message = message(text = "Debit 2500 on 03-NOV-2026 08:30")

            // when
            val parsed = parser.parse(message = message, catalog = catalog(template), zone = DOUALA)

            // then
            parsed?.occurredAt shouldBe Instant.parse("2026-11-03T07:30:00Z")
        }
    }

    private fun template(
        id: String = "test.payment",
        senders: List<String> = listOf("TestMoney"),
        kind: MessageKind = MessageKind.PAYMENT,
        pattern: String,
        dateFormat: String? = null,
        dateLocale: String = "fr",
    ) = MessageTemplate(
        id = id,
        version = 1,
        senders = senders,
        kind = kind,
        direction = Direction.DEBIT,
        currency = "XAF",
        pattern = pattern,
        dateFormat = dateFormat,
        dateLocale = dateLocale,
    )

    private fun catalog(vararg templates: MessageTemplate) =
        TemplateCatalog(version = "test", templates = templates.toList())

    private fun message(packageName: String = "test.sms", title: String? = " testmoney ", text: String) =
        ReceivedMessage(packageName = packageName, title = title, text = text, receivedAt = RECEIVED_AT)

    private companion object {
        val DOUALA: ZoneId = ZoneId.of("Africa/Douala")
        val RECEIVED_AT: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
