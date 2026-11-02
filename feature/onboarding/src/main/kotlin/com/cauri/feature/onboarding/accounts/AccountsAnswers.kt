package com.cauri.feature.onboarding.accounts

import com.cauri.core.data.model.Bank
import com.cauri.core.data.model.MobileMoney
import com.cauri.core.data.model.knownMobileMoney

/**
 * The accounts declared during the onboarding, next to the Cash account that always exists.
 *
 * @property mobileMoney Mobile money services the user ticked, in the order of [knownMobileMoney].
 * @property banks Banks the user chose, in the order chosen; at most [MAX_BANKS].
 */
internal data class AccountsAnswers(val mobileMoney: List<MobileMoney>, val banks: List<Bank>)
