package com.cauri.core.capture.signal

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.RawSignal
import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.ParsedMessage
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DetectionServiceImplTest {
    private val service = DetectionServiceImpl()

    @Nested
    inner class Detect {
        @Test
        fun `should give the operation the template read on the account`() {
            // given
            val parsed = parsed(kind = MessageKind.PAYMENT, direction = Direction.DEBIT, currency = "XAF", fees = 50)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = emptyList(),
            )

            // then
            operation shouldBe DetectedOperation(
                rawSignalId = 7,
                templateVersion = "orange-money.merchant-payment@1",
                accountId = 2,
                type = DetectedType.EXPENSE,
                amount = 2_500,
                fee = 50,
                foreignAmount = null,
                foreignCurrency = null,
                balance = 12_500,
                counterparty = "BOULANGERIE",
                occurredAt = Instant.ofEpochSecond(1_000),
            )
        }

        @ParameterizedTest
        @CsvSource(
            "PAYMENT, DEBIT, EXPENSE",
            "TRANSFER, DEBIT, EXPENSE",
            "PAYMENT, CREDIT, INCOME",
            "TRANSFER, CREDIT, INCOME",
            "WITHDRAWAL, DEBIT, WITHDRAWAL",
            "DEPOSIT, CREDIT, DEPOSIT",
        )
        fun `should record what the message reports`(kind: MessageKind, direction: Direction, expected: DetectedType) {
            // given
            val parsed = parsed(kind = kind, direction = direction, currency = "XAF", fees = null)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = emptyList(),
            )

            // then
            operation.type shouldBe expected
        }

        @Test
        fun `should leave out a fee of zero`() {
            // given
            val parsed = parsed(kind = MessageKind.PAYMENT, direction = Direction.DEBIT, currency = "XAF", fees = 0)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = emptyList(),
            )

            // then
            operation.fee.shouldBeNull()
        }

        @Test
        fun `should keep aside the amount of an operation in another currency`() {
            // given
            val parsed = parsed(kind = MessageKind.PAYMENT, direction = Direction.DEBIT, currency = "USD", fees = 10)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = emptyList(),
            )

            // then
            Triple(operation.amount, operation.fee, operation.foreignAmount to operation.foreignCurrency) shouldBe
                Triple(0L, null, 2_500L to "USD")
        }

        @Test
        fun `should leave out the balance of an operation in another currency`() {
            // given
            val parsed = parsed(kind = MessageKind.PAYMENT, direction = Direction.DEBIT, currency = "USD", fees = null)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = emptyList(),
            )

            // then
            operation.balance.shouldBeNull()
        }

        @Test
        fun `should make a transfer to the user's own account the template names`() {
            // given
            val parsed = parsed(kind = MessageKind.TRANSFER, direction = Direction.DEBIT, currency = "XAF", fees = null)
                .copy(counterparty = "UBA UBA 221--407", ownAccount = true)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = listOf(orangeMoney(), uba()),
            )

            // then
            operation.type shouldBe DetectedType.TRANSFER_OUT
            operation.counterpartAccountId shouldBe 3
        }

        @Test
        fun `should keep an expense when the account named is not followed`() {
            // given
            val parsed = parsed(kind = MessageKind.TRANSFER, direction = Direction.DEBIT, currency = "XAF", fees = null)
                .copy(counterparty = "BICEC 0123", ownAccount = true)

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = listOf(orangeMoney(), uba()),
            )

            // then
            operation.type shouldBe DetectedType.EXPENSE
        }

        @Test
        fun `should never take a person for an account of the user's own`() {
            // given
            val parsed = parsed(kind = MessageKind.TRANSFER, direction = Direction.DEBIT, currency = "XAF", fees = null)
                .copy(counterparty = "KOUBA MARTIN")

            // when
            val operation = service.detect(
                signal = signal(),
                parsed = parsed,
                account = orangeMoney(),
                accounts = listOf(orangeMoney(), uba()),
            )

            // then
            operation.type shouldBe DetectedType.EXPENSE
        }
    }

    private fun signal() = RawSignal(
        id = 7,
        sender = "com.samsung.android.messaging",
        title = "OrangeMoney",
        text = "Paiement",
        receivedAt = Instant.ofEpochSecond(1_000),
    )

    private fun uba() = Account(id = 3, name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF")

    private fun orangeMoney() = Account(
        id = 2,
        name = "Orange Money",
        kind = AccountKind.MOBILE_MONEY,
        provider = "ORANGE_MONEY",
        currency = "XAF",
    )

    private fun parsed(kind: MessageKind, direction: Direction, currency: String, fees: Long?) = ParsedMessage(
        templateId = "orange-money.merchant-payment",
        templateVersion = 1,
        provider = "ORANGE_MONEY",
        kind = kind,
        direction = direction,
        amount = 2_500,
        currency = currency,
        fees = fees,
        balance = 12_500,
        counterparty = "BOULANGERIE",
        reference = null,
        occurredAt = Instant.ofEpochSecond(1_000),
    )
}
