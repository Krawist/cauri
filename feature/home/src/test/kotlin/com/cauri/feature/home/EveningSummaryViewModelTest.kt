package com.cauri.feature.home

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.repository.FakeActivityRepository
import com.cauri.core.data.service.FakeActivityQueryService
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class EveningSummaryViewModelTest {
    private val activity = FakeActivityRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should total what was spent today, and of it in cash`() = runTest {
            // given
            activity.lines.value = listOf(
                payment(id = 1, at = "2026-11-03T08:00:00Z", kind = AccountKind.CASH),
                payment(id = 2, at = "2026-11-03T12:00:00Z", kind = AccountKind.MOBILE_MONEY),
                payment(id = 3, at = "2026-11-02T20:00:00Z", kind = AccountKind.CASH),
            )
            val viewModel = EveningSummaryViewModel(
                activity = activity,
                queries = FakeActivityQueryService(),
                clock = Clock.fixed(Instant.parse("2026-11-03T19:30:00Z"), ZoneId.of("Africa/Douala")),
            )

            // when
            val state = viewModel.uiState.first { it.spent > 0 }

            // then
            state shouldBe EveningSummaryUiState(day = LocalDate.of(2026, 11, 3), spent = 5_000, cash = 2_500)
        }
    }

    private fun payment(id: Long, at: String, kind: AccountKind) = ActivityLine.Operation(
        at = Instant.parse(at),
        transactionId = id,
        type = PendingType.EXPENSE,
        status = ActivityStatus.CONFIRMED,
        amount = 2_500,
        accountName = kind.name,
        accountKind = kind,
        toAccountName = null,
        toAccountKind = null,
        categoryName = null,
        parts = 1,
        counterparty = null,
        note = null,
        fees = 0,
        fee = false,
        reconciled = false,
    )
}
