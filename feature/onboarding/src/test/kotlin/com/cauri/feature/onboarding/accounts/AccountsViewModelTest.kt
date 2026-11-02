package com.cauri.feature.onboarding.accounts

import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.model.knownMobileMoney
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class AccountsViewModelTest {
    private val viewModel = AccountsViewModel()
    private val mtnMomo = knownMobileMoney.single { it.code == "MTN_MOMO" }
    private val orangeMoney = knownMobileMoney.single { it.code == "ORANGE_MONEY" }

    @Nested
    inner class OnMobileMoneyToggle {
        @Test
        fun `should tick a service`() {
            // when
            viewModel.onMobileMoneyToggle(orangeMoney)

            // then
            viewModel.uiState.value.mobileMoney shouldBe setOf(orangeMoney)
        }

        @Test
        fun `should untick a ticked service`() {
            // given
            viewModel.onMobileMoneyToggle(orangeMoney)

            // when
            viewModel.onMobileMoneyToggle(orangeMoney)

            // then
            viewModel.uiState.value.mobileMoney.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnBankPickerOpen {
        @Test
        fun `should open the list with every bank`() {
            // given
            viewModel.onBankQueryChange("bicec")

            // when
            viewModel.onBankPickerOpen()

            // then
            viewModel.uiState.value.pickingBank.shouldBeTrue()
            viewModel.uiState.value.bankResults shouldContainExactly knownBanks
        }
    }

    @Nested
    inner class OnBankPickerClose {
        @Test
        fun `should close the list and keep the chosen banks`() {
            // given
            viewModel.onBankPickerOpen()
            viewModel.onBankToggle(knownBanks[0])

            // when
            viewModel.onBankPickerClose()

            // then
            viewModel.uiState.value.pickingBank.shouldBeFalse()
            viewModel.uiState.value.banks shouldContainExactly listOf(knownBanks[0])
        }
    }

    @Nested
    inner class OnBankQueryChange {
        @ParameterizedTest
        @ValueSource(strings = ["societe", "SOCIÉTÉ", "  Générale "])
        fun `should find a bank whatever the case and accents`(query: String) {
            // when
            viewModel.onBankQueryChange(query)

            // then
            viewModel.uiState.value.bankResults.map { it.name } shouldContainExactly listOf("Société Générale")
        }

        @Test
        fun `should find no bank for an unknown name`() {
            // when
            viewModel.onBankQueryChange("Banque inconnue")

            // then
            viewModel.uiState.value.bankResults.shouldBeEmpty()
        }

        @Test
        fun `should list every bank for an empty search`() {
            // given
            viewModel.onBankQueryChange("bicec")

            // when
            viewModel.onBankQueryChange("")

            // then
            viewModel.uiState.value.bankResults shouldContainExactly knownBanks
        }
    }

    @Nested
    inner class OnBankToggle {
        @Test
        fun `should choose several banks in the order chosen`() {
            // when
            viewModel.onBankToggle(knownBanks[2])
            viewModel.onBankToggle(knownBanks[0])

            // then
            viewModel.uiState.value.banks shouldContainExactly listOf(knownBanks[2], knownBanks[0])
        }

        @Test
        fun `should give up a chosen bank`() {
            // given
            viewModel.onBankToggle(knownBanks[0])
            viewModel.onBankToggle(knownBanks[1])

            // when
            viewModel.onBankToggle(knownBanks[0])

            // then
            viewModel.uiState.value.banks shouldContainExactly listOf(knownBanks[1])
        }

        @Test
        fun `should not choose more than three banks`() {
            // given
            knownBanks.take(MAX_BANKS).forEach(viewModel::onBankToggle)

            // when
            viewModel.onBankToggle(knownBanks[MAX_BANKS])

            // then
            viewModel.uiState.value.banks shouldContainExactly knownBanks.take(MAX_BANKS)
        }
    }

    @Nested
    inner class OnContinue {
        @Test
        fun `should give no account but Cash when nothing is ticked`() {
            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe AccountsAnswers(mobileMoney = emptyList(), banks = emptyList())
        }

        @Test
        fun `should give the services in their order and the banks in the order chosen`() {
            // given
            viewModel.onMobileMoneyToggle(orangeMoney)
            viewModel.onMobileMoneyToggle(mtnMomo)
            viewModel.onBankToggle(knownBanks[2])
            viewModel.onBankToggle(knownBanks[0])

            // when
            viewModel.onContinue()

            // then
            viewModel.uiState.value.answered shouldBe AccountsAnswers(
                mobileMoney = listOf(mtnMomo, orangeMoney),
                banks = listOf(knownBanks[2], knownBanks[0]),
            )
        }
    }

    @Nested
    inner class OnAnswersHandedOn {
        @Test
        fun `should not give the accounts again when the user comes back to the step`() {
            // given
            viewModel.onContinue()

            // when
            viewModel.onAnswersHandedOn()

            // then
            viewModel.uiState.value.answered.shouldBeNull()
        }
    }
}
