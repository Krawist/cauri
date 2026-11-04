package com.cauri.core.parsing

import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZoneId
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/** The bundled templates (`message_templates.json`) against the anonymised messages collected on real phones. */
internal class MessageTemplatesTest {
    private val parser = MessageParserImpl()

    @Nested
    inner class Read {
        @Test
        fun `should accept every bundled template`() {
            // when
            val catalog = TemplateCatalogReaderImpl().read(resource(TEMPLATES))

            // then
            catalog.templates.filter { it.version < 1 }.shouldBeEmpty()
        }
    }

    @Nested
    inner class Parse {
        @ParameterizedTest(name = "{0} #{index}")
        @MethodSource("com.cauri.core.parsing.MessageTemplatesTest#operations")
        fun `should read every collected operation`(message: CollectedMessage) {
            // when
            val parsed = parser.parse(message = received(message), catalog = catalog, zone = DOUALA)

            // then
            parsed shouldBe message.expected?.let {
                ParsedMessage(
                    templateId = it.templateId,
                    templateVersion = 1,
                    provider = catalog.templates.single { template -> template.id == it.templateId }.provider,
                    kind = it.kind,
                    direction = it.direction,
                    amount = it.amount,
                    currency = it.currency,
                    fees = it.fees,
                    balance = it.balance,
                    counterparty = it.counterparty,
                    reference = it.reference,
                    occurredAt = Instant.parse(it.occurredAt),
                )
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.cauri.core.parsing.MessageTemplatesTest#others")
        fun `should recognise no other collected message`(message: CollectedMessage) {
            // when
            val parsed = parser.parse(message = received(message), catalog = catalog, zone = DOUALA)

            // then
            parsed.shouldBeNull()
        }
    }

    private fun received(message: CollectedMessage) = ReceivedMessage(
        packageName = message.packageName ?: "com.samsung.android.messaging",
        title = message.sender,
        text = message.text,
        receivedAt = RECEIVED_AT,
    )

    companion object {
        private const val TEMPLATES = "/message_templates.json"
        private const val MESSAGES = "/collected_messages.json"
        private val DOUALA: ZoneId = ZoneId.of("Africa/Douala")

        // Time of every collected notification; the messages that carry a date are read at it instead.
        private val RECEIVED_AT: Instant = Instant.parse("2026-01-15T10:00:00Z")

        private val catalog: TemplateCatalog by lazy { TemplateCatalogReaderImpl().read(resource(TEMPLATES)) }

        private val messages: List<CollectedMessage> by lazy {
            Json.decodeFromString(ListSerializer(CollectedMessage.serializer()), resource(MESSAGES))
        }

        @JvmStatic
        fun operations(): List<Arguments> = messages
            .mapNotNull { message -> message.expected?.let { Arguments.of(Named.of(it.templateId, message)) } }

        @JvmStatic
        fun others(): List<Arguments> = messages
            .filter { it.expected == null }
            .map { Arguments.of(Named.of(it.text.take(PREVIEW), it)) }

        private const val PREVIEW = 40

        private fun resource(name: String): String =
            checkNotNull(MessageTemplatesTest::class.java.getResource(name)) { "Missing $name" }.readText()
    }
}
