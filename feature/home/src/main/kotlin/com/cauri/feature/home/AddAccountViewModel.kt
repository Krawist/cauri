package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.model.knownMobileMoney
import com.cauri.core.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Diacritics left by the decomposition, removed so that « societe » finds « Société ».
private val Diacritics = Regex("\\p{Mn}+")

/**
 * « Ajouter un compte », from « Mes comptes »: the mobile money services, then the banks, the user does not follow yet,
 * searchable; the ones ticked are followed at once.
 */
@HiltViewModel
internal class AddAccountViewModel @Inject constructor(private val accounts: AccountRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val chosen = MutableStateFlow(emptySet<String>())
    private val added = MutableStateFlow(false)

    val uiState: StateFlow<AddAccountUiState> =
        combine(accounts.accounts, query, chosen, added) { followed, query, chosen, added ->
            val providers = followed.map { it.provider }.toSet()
            val search = query.searchable()
            AddAccountUiState(
                query = query,
                choices = (mobileMoney + banks).filter {
                    it.provider !in providers && it.name.searchable().contains(search)
                },
                chosen = chosen,
                added = added,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_DELAY),
            initialValue = AddAccountUiState(),
        )

    /** Searches the list for [query] as it is typed. */
    fun onQuery(query: String) {
        this.query.value = query
    }

    /** Ticks [choice], or unticks it. */
    fun onToggle(choice: AccountChoice) = chosen.update {
        if (choice.provider in it) it - choice.provider else it + choice.provider
    }

    /** Follows every choice ticked, then closes. */
    fun onAdd() {
        val picked = (mobileMoney + banks).filter { it.provider in chosen.value }
        viewModelScope.launch {
            // An account the user once hid keeps its provider: it is never created twice.
            picked.filter { accounts.accountOf(it.provider) == null }.forEach {
                accounts.createAccount(
                    NewAccount(name = it.name, kind = it.kind, provider = it.provider, currency = XAF),
                )
            }
            added.value = true
        }
    }

    /** Forgets the search and the choices, once the sheet closed. */
    fun onClosed() {
        query.value = ""
        chosen.value = emptySet()
        added.value = false
    }

    // Lower case, without accents nor surrounding spaces.
    private fun String.searchable() =
        Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Diacritics, "")

    private companion object {
        const val STOP_DELAY = 5_000L

        // The accounts of Cameroon count in CFA francs, as at the onboarding.
        const val XAF = "XAF"

        val mobileMoney = knownMobileMoney.map {
            AccountChoice(
                provider = it.code,
                name = it.name,
                description = it.description,
                kind = AccountKind.MOBILE_MONEY,
            )
        }

        val banks = knownBanks.sortedBy { it.name.lowercase() }.map {
            AccountChoice(provider = it.code, name = it.name, description = it.description, kind = AccountKind.BANK)
        }
    }
}
