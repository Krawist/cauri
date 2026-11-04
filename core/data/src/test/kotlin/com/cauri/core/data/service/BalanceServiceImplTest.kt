package com.cauri.core.data.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class BalanceServiceImplTest {
    private val service = BalanceServiceImpl()

    @Nested
    inner class AfterOperation {
        @Test
        fun `should keep the balance the message read`() {
            // when & then
            service.afterOperation(read = 948_050, previous = 1_000_000, change = -51_950) shouldBe 948_050
        }

        @ParameterizedTest
        @CsvSource("-51950, 948050", "223066, 1223066")
        fun `should move a balance the bank forgot to update by the operation`(change: Long, expected: Long) {
            // when & then
            service.afterOperation(read = 1_000_000, previous = 1_000_000, change = change) shouldBe expected
        }

        @Test
        fun `should keep the balance read when nothing was known before`() {
            // when & then
            service.afterOperation(read = 1_000_000, previous = null, change = -51_950) shouldBe 1_000_000
        }

        @Test
        fun `should keep the balance read when the operation moved nothing known`() {
            // when & then
            service.afterOperation(read = 1_000_000, previous = 1_000_000, change = 0) shouldBe 1_000_000
        }
    }
}
