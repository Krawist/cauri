package com.cauri.feature.onboarding.accounts

import androidx.lifecycle.ViewModel
import com.cauri.core.data.model.Bank
import com.cauri.core.data.model.MobileMoney
import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.model.knownMobileMoney
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Diacritics left by the decomposition, removed so that « societe » finds « Société ».
private val Diacritics = Regex("\\p{Mn}+")

/** Accounts step of the onboarding: the mobile money services and up to [MAX_BANKS] banks the user uses. */
@HiltViewModel
internal class AccountsViewModel @Inject constructor() :
    ViewModel(),
    AccountsActions {
    private val state = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = state.asStateFlow()

    override fun onMobileMoneyToggle(service: MobileMoney) = state.update {
        it.copy(mobileMoney = if (service in it.mobileMoney) it.mobileMoney - service else it.mobileMoney + service)
    }

    override fun onBankPickerOpen() = state.update {
        it.copy(pickingBank = true, bankQuery = "", bankResults = knownBanks)
    }

    override fun onBankPickerClose() = state.update { it.copy(pickingBank = false) }

    override fun onBankQueryChange(query: String) = state.update {
        val searched = query.searchable()
        it.copy(bankQuery = query, bankResults = knownBanks.filter { bank -> searched in bank.name.searchable() })
    }

    override fun onBankToggle(bank: Bank) = state.update {
        val banks = when {
            bank in it.banks -> it.banks - bank
            it.banks.size < MAX_BANKS -> it.banks + bank
            else -> it.banks
        }
        it.copy(banks = banks)
    }

    override fun onContinue() = state.update {
        it.copy(
            answered = AccountsAnswers(
                knownMobileMoney.filter { service ->
                    service in it.mobileMoney
                },
                it.banks,
            ),
        )
    }

    /** The screen handed the accounts on: coming back to this step must not hand them on again by itself. */
    fun onAnswersHandedOn() = state.update { it.copy(answered = null) }

    // Lower case, without accents nor surrounding spaces.
    private fun String.searchable() =
        Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Diacritics, "")
}
