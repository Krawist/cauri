package com.cauri.feature.onboarding.profile

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ProfileViewModelTest {
    private val viewModel = ProfileViewModel()

    @Nested
    inner class OnContinue {
        @Test
        fun `should point out every missing answer on a first attempt`() {
            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder ProfileField.entries
        }

        @Test
        fun `should point out a first name made of spaces`() {
            // given
            viewModel.onFirstNameChange("   ")
            viewModel.onOccupationSelect(Occupation.STUDENT)
            viewModel.onIncomeTypeSelect(IncomeType.VARIABLE)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder listOf(ProfileField.FIRST_NAME)
        }

        @Test
        fun `should give no answers while one is missing`() {
            // given
            viewModel.onFirstNameChange("Krawist")
            viewModel.onOccupationSelect(Occupation.TRADER)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }

        @Test
        fun `should give the answers with the first name trimmed`() {
            // given
            viewModel.onFirstNameChange("  Krawist ")
            viewModel.onOccupationSelect(Occupation.TRADER)
            viewModel.onIncomeTypeSelect(IncomeType.FIXED_DATE)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe
                ProfileAnswers(
                    displayName = "Krawist",
                    occupation = Occupation.TRADER,
                    incomeType = IncomeType.FIXED_DATE,
                )
        }
    }

    @Nested
    inner class OnAnswersHandedOn {
        @Test
        fun `should not give the answers again when the user comes back to the step`() {
            // given
            viewModel.onFirstNameChange("Krawist")
            viewModel.onOccupationSelect(Occupation.TRADER)
            viewModel.onIncomeTypeSelect(IncomeType.FIXED_DATE)
            viewModel.onContinue()

            // when
            viewModel.onAnswersHandedOn()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }
    }

    @Nested
    inner class OnFirstNameChange {
        @Test
        fun `should not point out missing answers before a first attempt`() {
            // when
            viewModel.onFirstNameChange("")

            // then
            viewModel.uiState.value.missingFields.shouldBeEmpty()
        }

        @Test
        fun `should stop pointing out the first name once typed after an attempt`() {
            // given
            viewModel.onContinue()

            // when
            viewModel.onFirstNameChange("Krawist")

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder
                listOf(ProfileField.OCCUPATION, ProfileField.INCOME_TYPE)
        }
    }

    @Nested
    inner class OnOccupationSelect {
        @Test
        fun `should stop pointing out the occupation once chosen after an attempt`() {
            // given
            viewModel.onContinue()

            // when
            viewModel.onOccupationSelect(Occupation.EMPLOYEE)

            // then
            viewModel.uiState.value.missingFields shouldContainExactlyInAnyOrder
                listOf(ProfileField.FIRST_NAME, ProfileField.INCOME_TYPE)
        }
    }

    @Nested
    inner class OnIncomeTypeSelect {
        @Test
        fun `should be complete once the last answer is given`() {
            // given
            viewModel.onFirstNameChange("Krawist")
            viewModel.onOccupationSelect(Occupation.TRADER)

            // when
            viewModel.onIncomeTypeSelect(IncomeType.VARIABLE)

            // then
            viewModel.uiState.value.complete.shouldBeTrue()
        }

        @Test
        fun `should not be complete while the first name is blank`() {
            // given
            viewModel.onFirstNameChange("  ")
            viewModel.onOccupationSelect(Occupation.TRADER)

            // when
            viewModel.onIncomeTypeSelect(IncomeType.VARIABLE)

            // then
            viewModel.uiState.value.complete.shouldBeFalse()
        }
    }
}
