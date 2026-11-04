package com.cauri.core.data.service

import io.kotest.matchers.shouldBe
import java.time.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AvailableServiceImplTest {
    private val service = AvailableServiceImpl()

    @Nested
    inner class PerDay {
        @Test
        fun `should divide what is left by the days until the end of the month, today included, down to 100 F`() {
            // given
            val month = MonthMoney(received = 200_000, savings = 20_000, unpaidFixedCosts = 50_000, spent = 30_000)

            // when
            val available = service.perDay(month, today = LocalDate.parse("2026-11-04"))

            // then
            available shouldBe 3_700
        }

        @ParameterizedTest
        @CsvSource("2026-11-30, 10000", "2026-02-01, 300")
        fun `should count the days left in the month of today`(today: String, expected: Long) {
            // given
            val month = MonthMoney(received = 10_000, savings = 0, unpaidFixedCosts = 0, spent = 0)

            // when & then
            service.perDay(month, today = LocalDate.parse(today)) shouldBe expected
        }

        @Test
        fun `should never be below 0`() {
            // given
            val month = MonthMoney(received = 50_000, savings = 5_000, unpaidFixedCosts = 50_000, spent = 10_000)

            // when & then
            service.perDay(month, today = LocalDate.parse("2026-11-04")) shouldBe 0
        }
    }
}
