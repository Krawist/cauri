package com.cauri.feature.transactions.activity

import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivitySort

/** What the user does on « Activité ». */
internal interface ActivityActions {
    /** Shows the accounts of [filter]. */
    fun onFilter(filter: ActivityFilter)

    /** Opens the search, or closes it and forgets what it looked for. */
    fun onSearch(open: Boolean)

    /** Looks for [query] as it is typed. */
    fun onQuery(query: String)

    /** Shows the days of [period]. */
    fun onPeriod(period: ActivityPeriod)

    /** Orders the lines by [sort]. */
    fun onSort(sort: ActivitySort)
}
