package com.cauri.core.data.service

import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.Spending
import java.time.LocalDate

/** Narrows « Activité »: the search, the period and what was spent over it. */
interface ActivityQueryService {
    /**
     * Whether [line] answers [query]: its merchant, note, category or accounts contain it, accents and case
     * ignored, or, for a query of digits only (« 5000 » as « 5 000 »), its amount does. A blank query matches every
     * line.
     */
    fun matches(line: ActivityLine, query: String): Boolean

    /** The days of [period] around [today], both ends included; `null` for the whole history. */
    fun days(period: ActivityPeriod, today: LocalDate): ClosedRange<LocalDate>?

    /**
     * What [lines] spent, cash apart from digital: the expenses and the fees of the transfers, never what was set
     * aside.
     */
    fun spending(lines: List<ActivityLine>): Spending
}
