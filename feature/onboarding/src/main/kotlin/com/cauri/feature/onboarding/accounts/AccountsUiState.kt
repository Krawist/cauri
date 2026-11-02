package com.cauri.feature.onboarding.accounts

import com.cauri.core.data.model.Bank
import com.cauri.core.data.model.MobileMoney
import com.cauri.core.data.model.knownBanks

/** Banks the user can declare during the onboarding; more later from the settings. */
internal const val MAX_BANKS = 3

/**
 * State of the accounts screen. The step is always complete: the Cash account is enough.
 *
 * @property mobileMoney Mobile money services the user ticked.
 * @property banks Banks the user chose, in the order chosen; at most [MAX_BANKS].
 * @property pickingBank Whether the list of banks is open.
 * @property bankQuery Text typed to search the list of banks.
 * @property bankResults Banks matching [bankQuery], in the order of [knownBanks].
 * @property answered The declared accounts, once the user continued, for the screen to hand on; `null` otherwise.
 */
internal data class AccountsUiState(
    val mobileMoney: Set<MobileMoney> = emptySet(),
    val banks: List<Bank> = emptyList(),
    val pickingBank: Boolean = false,
    val bankQuery: String = "",
    val bankResults: List<Bank> = knownBanks,
    val answered: AccountsAnswers? = null,
)
