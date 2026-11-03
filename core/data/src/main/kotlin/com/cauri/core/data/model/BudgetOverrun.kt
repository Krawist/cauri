package com.cauri.core.data.model

/**
 * A budget gone over, or that the month will go over at the pace so far.
 *
 * @property name Name of the category.
 * @property over How far beyond its budget the category is already, in minor units of XAF; 0 while it is not.
 * @property projected What the category will cost by the end of the month at the pace so far, rounded to 500 F.
 */
data class BudgetOverrun(val name: String, val over: Long, val projected: Long)
