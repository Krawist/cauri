package com.cauri.core.data.model

/**
 * What the user spent since the start of the month, the money set aside left out.
 *
 * @property cash Expenses on the Cash account, in minor units of XAF.
 * @property digital Expenses on the mobile money, card and bank accounts, in minor units of XAF.
 */
data class MonthSpending(val cash: Long, val digital: Long)
