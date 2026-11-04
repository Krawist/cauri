package com.cauri.core.parsing.freetext

/**
 * An expense read in a free text the user typed, such as « taxi 1500 » in « taxi 1500, beignets 500 ».
 *
 * @property label What was paid, as the user wrote it (« taxi »).
 * @property amount Amount in FCFA, written in the text as it is: the model never computes it.
 * @property category Name of the user's category the model chose; `null` when it chose none of them.
 */
data class FreeTextExpense(val label: String, val amount: Long, val category: String?)
