package com.cauri.core.capture.signal

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SignalFilterServiceImplTest {
    private val service = SignalFilterServiceImpl()

    // Made-up package names and senders: the rule, not the list, is tested here.
    private val watched = WatchedSenders(
        apps = listOf(
            WatchedApp(packageName = "test.bank", name = "Bank", status = WatchedStatus.CONFIRMED, checkedOn = ""),
        ),
        messageApps = listOf(
            WatchedApp(packageName = "test.sms", name = "SMS", status = WatchedStatus.CONFIRMED, checkedOn = ""),
        ),
        messageSenders = listOf(
            WatchedMessageSender(name = "MobileMoney", status = WatchedStatus.CONFIRMED, checkedOn = ""),
        ),
    )

    @Nested
    inner class Keeps {
        @Test
        fun `should keep any notification of a watched app`() {
            // when
            val kept = service.keeps(watched = watched, packageName = "test.bank", title = "Anything")

            // then
            kept.shouldBeTrue()
        }

        @Test
        fun `should keep the message of a watched sender in a message app, whatever its case and spaces`() {
            // when
            val kept = service.keeps(watched = watched, packageName = "test.sms", title = " mobilemoney ")

            // then
            kept.shouldBeTrue()
        }

        @Test
        fun `should never keep the user's own messages`() {
            // when
            val kept = service.keeps(watched = watched, packageName = "test.sms", title = "Maman")

            // then
            kept.shouldBeFalse()
        }

        @Test
        fun `should never keep an SMS without sender`() {
            // when
            val kept = service.keeps(watched = watched, packageName = "test.sms", title = null)

            // then
            kept.shouldBeFalse()
        }

        @Test
        fun `should never keep the notifications of other apps`() {
            // when
            val kept = service.keeps(watched = watched, packageName = "test.chat", title = "MobileMoney")

            // then
            kept.shouldBeFalse()
        }
    }
}
