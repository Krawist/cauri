package com.cauri.feature.onboarding.profile

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.UserProfile
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
internal class ProfileViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private lateinit var viewModel: ProfileViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ProfileViewModel(preferences)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
        fun `should not save the profile while an answer is missing`() {
            // given
            viewModel.onFirstNameChange("Krawist")
            viewModel.onOccupationSelect(Occupation.TRADER)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.saved.shouldBeFalse()
            preferences.profile.value.shouldBeNull()
        }

        @Test
        fun `should save the profile with the first name trimmed`() {
            // given
            viewModel.onFirstNameChange("  Krawist ")
            viewModel.onOccupationSelect(Occupation.TRADER)
            viewModel.onIncomeTypeSelect(IncomeType.FIXED_DATE)

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.saved.shouldBeTrue()
            preferences.profile.value shouldBe
                UserProfile(displayName = "Krawist", occupation = Occupation.TRADER, incomeType = IncomeType.FIXED_DATE)
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
}
