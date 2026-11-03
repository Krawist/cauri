package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.repository.FakeActivityRepository
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ActivityViewModelTest {
    private val activity = FakeActivityRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: ActivityViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ActivityViewModel(activity, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should group the lines by day, newest first`() = runTest {
            // given
            viewModel.uiState.launchIn(backgroundScope)
            activity.lines.value = listOf(
                payment(id = 1, at = "2026-11-03T08:00:00Z", kind = AccountKind.CASH),
                payment(id = 2, at = "2026-11-02T20:00:00Z", kind = AccountKind.CASH),
            )

            // when
            val days = viewModel.uiState.first { it.days.isNotEmpty() }.days

            // then
            days.map { it.date } shouldContainExactly listOf(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 2))
        }
    }

    @Nested
    inner class OnFilter {
        @Test
        fun `should show a withdrawal with the cash, and leave out the bank`() = runTest {
            // given
            viewModel.uiState.launchIn(backgroundScope)
            activity.lines.value = listOf(
                payment(id = 1, at = "2026-11-03T08:00:00Z", kind = AccountKind.BANK),
                payment(id = 2, at = "2026-11-03T09:00:00Z", kind = AccountKind.MOBILE_MONEY)
                    .copy(type = PendingType.TRANSFER, toAccountName = "Cash", toAccountKind = AccountKind.CASH),
            )

            // when
            viewModel.onFilter(ActivityFilter.CASH)

            // then
            val state = viewModel.uiState.first { it.filter == ActivityFilter.CASH }
            state.days.flatMap { it.lines }.map { (it as ActivityLine.Operation).transactionId } shouldContainExactly
                listOf(2L)
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
