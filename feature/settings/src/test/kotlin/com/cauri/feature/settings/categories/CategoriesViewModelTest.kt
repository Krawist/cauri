package com.cauri.feature.settings.categories

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.repository.FakeCategoryRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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
internal class CategoriesViewModelTest {
    private val repository = FakeCategoryRepository()
    private lateinit var viewModel: CategoriesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = CategoriesViewModel(
            repository = repository,
            clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnDraftSave {
        @Test
        fun `should create a category of the tab shown`() {
            // given
            viewModel.onNature(CategoryNature.INCOME)
            viewModel.onEdit(null)
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(name = "Ventes"))

            // when
            viewModel.onDraftSave()

            // then
            repository.created shouldContainExactly listOf(
                NewCategory(
                    name = "Ventes",
                    nature = CategoryNature.INCOME,
                    icon = CategoryIcon.TAG,
                    color = CategoryColor.LAGOON,
                ),
            )
            viewModel.uiState.value.draft.shouldBeNull()
        }

        @Test
        fun `should point out the name missing`() {
            // given
            viewModel.onEdit(null)

            // when
            viewModel.onDraftSave()

            // then
            viewModel.uiState.value.draft?.nameMissing shouldBe true
            repository.created.shouldBeEmpty()
        }

        @Test
        fun `should rename and dress a category edited`() {
            // given
            viewModel.onEdit(TRANSPORT)
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(icon = CategoryIcon.HOUSE))

            // when
            viewModel.onDraftSave()

            // then
            repository.updated shouldContainExactly listOf(
                FakeCategoryRepository.Updated(
                    id = 3,
                    name = "Transport",
                    icon = CategoryIcon.HOUSE,
                    color = CategoryColor.GOLD,
                ),
            )
        }
    }

    @Nested
    inner class OnAddSub {
        @Test
        fun `should add the sub-category typed to the category of the sheet`() {
            // given
            viewModel.onEdit(TRANSPORT)
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(newSub = " Taxi "))

            // when
            viewModel.onAddSub()

            // then
            repository.created.map { it.name to it.parentId } shouldContainExactly listOf("Taxi" to 3L)
            viewModel.uiState.value.draft?.newSub shouldBe ""
        }
    }

    @Nested
    inner class OnMerge {
        @Test
        fun `should merge the category of the sheet into the one chosen`() {
            // given
            viewModel.onEdit(TRANSPORT)
            viewModel.onAskMerge(true)

            // when
            viewModel.onMerge(into = 7)

            // then
            repository.merges shouldContainExactly listOf(3L to 7L)
            viewModel.uiState.value.draft.shouldBeNull()
        }
    }

    @Nested
    inner class OnArchive {
        @Test
        fun `should archive the category of the sheet`() {
            // given
            viewModel.onEdit(TRANSPORT)

            // when
            viewModel.onArchive()

            // then
            repository.archivedIds shouldContainExactly listOf(3L)
        }
    }

    @Nested
    inner class OnToggle {
        @Test
        fun `should open then close the sub-categories`() {
            // given
            viewModel.onToggle(3)

            // when
            viewModel.onToggle(3)

            // then
            viewModel.uiState.value.expanded.shouldBeEmpty()
        }
    }

    private companion object {
        val TRANSPORT = Category(
            id = 3,
            name = "Transport",
            nature = CategoryNature.EXPENSE,
            icon = CategoryIcon.TRANSPORT,
            color = CategoryColor.GOLD,
        )
    }
}
