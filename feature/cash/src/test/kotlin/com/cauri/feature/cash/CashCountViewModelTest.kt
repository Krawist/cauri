package com.cauri.feature.cash

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.repository.FakeCashCountRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CashCountViewModelTest {
    private val cashCounts = FakeCashCountRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T20:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: CashCountViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        cashCounts.balance.value = CashBalance(amount = 7_500, countedAt = Instant.EPOCH, expensesSinceCount = 0)
        viewModel = CashCountViewModel(cashCounts, clock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnAdd {
        @Test
        fun `should add the banknotes and coins to the total`() {
            // when
            viewModel.onAdd(Denomination.NOTE_2000)
            viewModel.onAdd(Denomination.COIN_500)
            viewModel.onAdd(Denomination.COIN_100)

            // then
            viewModel.uiState.value.total shouldBe 2_600
        }

        @Test
        fun `should give the missing money as the gap`() {
            // when
            viewModel.onAdd(Denomination.NOTE_5000)

            // then
            viewModel.uiState.value.gap shouldBe 2_500
        }
    }

    @Nested
    inner class OnRemove {
        @Test
        fun `should never count below zero`() {
            // when
            viewModel.onRemove(Denomination.NOTE_1000)

            // then
            viewModel.uiState.value.counts[Denomination.NOTE_1000] shouldBe 0
        }
    }

    @Nested
    inner class OnClearAll {
        @Test
        fun `should start the count again from zero`() {
            // given
            viewModel.onAdd(Denomination.NOTE_10000)

            // when
            viewModel.onClearAll()

            // then
            viewModel.uiState.value.total shouldBe 0
        }
    }

    @Nested
    inner class OnTypedTotalChange {
        @Test
        fun `should take the typed total instead of the banknotes`() {
            // given
            viewModel.onAdd(Denomination.NOTE_10000)
            viewModel.onTypeTotal(true)

            // when
            viewModel.onTypedTotalChange("7 5a00")

            // then
            viewModel.uiState.value.total shouldBe 7_500
        }
    }

    @Nested
    inner class OnConfirm {
        @Test
        fun `should lead to the split when money is missing, without saving`() {
            // given
            viewModel.onAdd(Denomination.NOTE_5000)

            // when
            viewModel.onConfirm()

            // then
            viewModel.uiState.value.splitting.shouldBeTrue()
            cashCounts.counts.shouldBeEmpty()
        }

        @Test
        fun `should save the count when it adds up`() {
            // given
            viewModel.onAdd(Denomination.NOTE_5000)
            viewModel.onAdd(Denomination.NOTE_2000)
            viewModel.onAdd(Denomination.COIN_500)

            // when
            viewModel.onConfirm()

            // then
            cashCounts.counts shouldContainExactly listOf(
                FakeCashCountRepository.Count(amount = 7_500, countedAt = clock.instant(), expectedAmount = 7_500),
            )
            viewModel.uiState.value.saved.shouldBeTrue()
        }

        @Test
        fun `should save a surplus with the count`() {
            // given
            viewModel.onAdd(Denomination.NOTE_10000)

            // when
            viewModel.onConfirm()

            // then
            cashCounts.counts shouldContainExactly listOf(
                FakeCashCountRepository.Count(amount = 10_000, countedAt = clock.instant(), expectedAmount = 7_500),
            )
            viewModel.uiState.value.splitting.shouldBeFalse()
        }
    }
}
