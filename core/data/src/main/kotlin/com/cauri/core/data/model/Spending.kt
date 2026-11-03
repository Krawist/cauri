package com.cauri.core.data.model

/**
 * What the user spent over a period, the money set aside left out: the expenses and the fees of the transfers.
 *
 * @property cash Spent from the Cash account, in minor units of XAF.
 * @property digital Spent from the mobile money, card and bank accounts, in minor units of XAF.
 */
data class Spending(val cash: Long, val digital: Long)
