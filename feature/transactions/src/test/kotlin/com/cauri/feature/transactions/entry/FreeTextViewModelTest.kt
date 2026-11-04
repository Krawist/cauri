package com.cauri.feature.transactions.entry

import com.cauri.core.ai.FakeLanguageModel
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.parsing.freetext.FreeTextReaderImpl
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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
internal class FreeTextViewModelTest {
    private val model = FakeLanguageModel()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        categories.categories.value = listOf(Category(id = 4, name = "Transport", nature = CategoryNature.EXPENSE))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should hide the field without a model`() {
            // given
            model.available = false

            // when
            val viewModel = viewModel()

            // then
            viewModel.uiState.value.available.shouldBeFalse()
        }
    }

    @Nested
    inner class OnRead {
        @Test
        fun `should show the expenses read, their category among the user's`() {
            // given
            model.answer = """[{"label": "taxi", "amount": "1500", "category": "Transport"},
                {"label": "beignets", "amount": "500", "category": "Snacks"}]"""
            val viewModel = viewModel()
            viewModel.onText("taxi 1500, beignets 500")

            // when
            viewModel.onRead()

            // then
            viewModel.uiState.value.lines shouldContainExactly listOf(
                FreeTextLine(label = "taxi", amount = 1_500, categoryId = 4, categoryName = "Transport"),
                FreeTextLine(label = "beignets", amount = 500, categoryId = null, categoryName = null),
            )
            transactions.entries.shouldBeEmpty()
        }

        @Test
        fun `should tell when nothing with its amount was found`() {
            // given
            model.answer = """[{"label": "taxi", "amount": "2000", "category": null}]"""
            val viewModel = viewModel()
            viewModel.onText("taxi 1500")

            // when
            viewModel.onRead()

            // then
            viewModel.uiState.value.nothingFound.shouldBeTrue()
            viewModel.uiState.value.lines.shouldBeNull()
        }
    }

    @Nested
    inner class OnSave {
        @Test
        fun `should save the lines kept as expenses read in a free text`() {
            // given
            model.answer = """[{"label": "taxi", "amount": "1500", "category": "Transport"},
                {"label": "beignets", "amount": "500", "category": null}]"""
            val viewModel = viewModel()
            viewModel.onText("taxi 1500, beignets 500")
            viewModel.onRead()
            viewModel.onRemove(1)

            // when
            viewModel.onSave(accountId = 1)

            // then
            transactions.entries.map { Triple(it.amount, it.note, it.fromFreeText) } shouldContainExactly
                listOf(Triple(1_500L, "taxi", true))
            viewModel.uiState.value.saved.shouldBeTrue()
        }
    }

    private fun viewModel() = FreeTextViewModel(
        model = model,
        reader = FreeTextReaderImpl(),
        categories = categories,
        transactions = transactions,
        clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC),
    )
}
