package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.launch

/** The balance the user types for a digital account, from « Tes comptes » (CAU-52). */
@HiltViewModel
internal class BalanceViewModel @Inject constructor(
    private val accounts: AccountRepository,
    private val clock: Clock,
) : ViewModel() {
    /** Records [amount], in FCFA, as the balance of [accountId] now, the gap with the one shown left unsplit. */
    fun onSave(accountId: Long, amount: Long) {
        viewModelScope.launch { accounts.recordBalance(accountId = accountId, amount = amount, at = clock.instant()) }
    }
}
