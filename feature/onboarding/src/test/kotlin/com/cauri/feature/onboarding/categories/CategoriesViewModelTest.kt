package com.cauri.feature.onboarding.categories

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class CategoriesViewModelTest {
    private val viewModel = CategoriesViewModel()

    @Nested
    inner class UiState {
        @Test
        fun `should be complete when the screen opens`() {
            // when
            val complete = viewModel.uiState.value.complete

            // then
            complete.shouldBeTrue()
        }

        @Test
        fun `should keep the six categories of the mockup when the screen opens`() {
            // when
            val kept = viewModel.uiState.value.keptPack

            // then
            kept shouldContainExactlyInAnyOrder listOf(
                PackCategory.FOOD,
                PackCategory.TRANSPORT,
                PackCategory.AIRTIME_AND_DATA,
                PackCategory.FAMILY,
                PackCategory.TONTINE,
                PackCategory.HOUSING_AND_BILLS,
            )
        }
    }

    @Nested
    inner class OnPackCategoryToggle {
        @Test
        fun `should not be complete once the last kept category is removed`() {
            // when
            viewModel.uiState.value.keptPack.forEach(viewModel::onPackCategoryToggle)

            // then
            viewModel.uiState.value.complete.shouldBeFalse()
        }

        @Test
        fun `should be complete again once a category is kept`() {
            // given
            viewModel.uiState.value.keptPack.forEach(viewModel::onPackCategoryToggle)

            // when
            viewModel.onPackCategoryToggle(PackCategory.HEALTH)

            // then
            viewModel.uiState.value.complete.shouldBeTrue()
        }

        @Test
        fun `should remove a kept category`() {
            // when
            viewModel.onPackCategoryToggle(PackCategory.FAMILY)

            // then
            (PackCategory.FAMILY in viewModel.uiState.value.keptPack).shouldBeFalse()
        }

        @Test
        fun `should keep a removed category`() {
            // when
            viewModel.onPackCategoryToggle(PackCategory.HEALTH)

            // then
            (PackCategory.HEALTH in viewModel.uiState.value.keptPack).shouldBeTrue()
        }
    }

    @Nested
    inner class OnAddConfirm {
        @Test
        fun `should add the typed name without surrounding spaces`() {
            // given
            viewModel.onAddStart()
            viewModel.onNewNameChange("  Moto ")

            // when
            viewModel.onAddConfirm()

            // then
            viewModel.uiState.value.added shouldContainExactly listOf("Moto")
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "   ", "moto"])
        fun `should ignore a blank or already added name`(name: String) {
            // given
            viewModel.onNewNameChange("Moto")
            viewModel.onAddConfirm()
            viewModel.onNewNameChange(name)

            // when
            viewModel.onAddConfirm()

            // then
            viewModel.uiState.value.added shouldContainExactly listOf("Moto")
        }

        @Test
        fun `should close the field`() {
            // given
            viewModel.onAddStart()
            viewModel.onNewNameChange("Moto")

            // when
            viewModel.onAddConfirm()

            // then
            viewModel.uiState.value.adding.shouldBeFalse()
        }
    }

    @Nested
    inner class OnAddedCategoryRemove {
        @Test
        fun `should remove a category the user added`() {
            // given
            viewModel.onNewNameChange("Moto")
            viewModel.onAddConfirm()

            // when
            viewModel.onAddedCategoryRemove("Moto")

            // then
            viewModel.uiState.value.added shouldBe emptyList()
        }
    }

    @Nested
    inner class OnContinue {
        @Test
        fun `should give the kept categories in the order of the pack, then the added ones`() {
            // given
            viewModel.onPackCategoryToggle(PackCategory.HEALTH)
            viewModel.onNewNameChange("Moto")
            viewModel.onAddConfirm()

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe CategoriesAnswers(
                pack = listOf(
                    PackCategory.FOOD,
                    PackCategory.TRANSPORT,
                    PackCategory.AIRTIME_AND_DATA,
                    PackCategory.FAMILY,
                    PackCategory.TONTINE,
                    PackCategory.HOUSING_AND_BILLS,
                    PackCategory.HEALTH,
                ),
                added = listOf("Moto"),
            )
        }

        @Test
        fun `should point out that no category is kept`() {
            // given
            viewModel.uiState.value.keptPack.forEach(viewModel::onPackCategoryToggle)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.noneKept.shouldBeTrue()
            viewModel.uiState.value.answered.shouldBeNull()
        }

        @Test
        fun `should stop pointing it out once a category is kept again`() {
            // given
            viewModel.uiState.value.keptPack.forEach(viewModel::onPackCategoryToggle)
            viewModel.onContinue()

            // when
            viewModel.onPackCategoryToggle(PackCategory.FOOD)

            // then
            viewModel.uiState.value.noneKept.shouldBeFalse()
        }
    }

    @Nested
    inner class OnAnswersHandedOn {
        @Test
        fun `should not give the categories again when the user comes back to the step`() {
            // given
            viewModel.onContinue()

            // when
            viewModel.onAnswersHandedOn()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }
    }
}
