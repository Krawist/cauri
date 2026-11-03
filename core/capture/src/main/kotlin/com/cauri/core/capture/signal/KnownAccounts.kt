package com.cauri.core.capture.signal

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.model.knownMobileMoney
import com.cauri.core.data.model.knownPrepaidCards

private const val CURRENCY = "XAF"

/**
 * The account to create to follow [provider], named as the user would declare it; `null` when Cauri does not know
 * the provider.
 */
internal fun knownAccount(provider: String): NewAccount? {
    val mobileMoney = knownMobileMoney.firstOrNull { it.code == provider }?.let { it.code to it.name }
    val bank = knownBanks.firstOrNull { it.code == provider }?.let { it.code to it.name }
    val card = knownPrepaidCards.firstOrNull { it.code == provider }?.let { it.code to it.name }
    val kind = when {
        mobileMoney != null -> AccountKind.MOBILE_MONEY
        bank != null -> AccountKind.BANK
        else -> AccountKind.PREPAID_CARD
    }
    return (mobileMoney ?: bank ?: card)?.let { (code, name) ->
        NewAccount(name = name, kind = kind, provider = code, currency = CURRENCY)
    }
}
