package com.cauri.feature.onboarding.accounts

import com.cauri.core.data.model.Bank
import com.cauri.core.data.model.MobileMoney

/** What the user can do on the accounts screen and in its list of banks. */
internal interface AccountsActions {
    fun onMobileMoneyToggle(service: MobileMoney)

    /** Opens the list of banks, with an empty search. */
    fun onBankPickerOpen()

    fun onBankPickerClose()

    fun onBankQueryChange(query: String)

    /** Chooses [bank], or gives it up when already chosen; nothing past [MAX_BANKS]. */
    fun onBankToggle(bank: Bank)

    fun onContinue()
}
