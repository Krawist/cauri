package com.cauri.core.data.service

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.Spending
import java.time.LocalDate

/**
 * [ActivityQueryService] for tests: a line answers a query its merchant contains, a period is the days the test sets
 * in [periods], and the spending adds up the expenses.
 */
class FakeActivityQueryService : ActivityQueryService {
    /** Days each period covers; a period missing is the whole history. */
    val periods = mutableMapOf<ActivityPeriod, ClosedRange<LocalDate>>()

    override fun matches(line: ActivityLine, query: String): Boolean =
        query.isBlank() || (line as? ActivityLine.Operation)?.counterparty?.contains(query) == true

    override fun days(period: ActivityPeriod, today: LocalDate): ClosedRange<LocalDate>? = periods[period]

    override fun spending(lines: List<ActivityLine>): Spending {
        val expenses = lines.filterIsInstance<ActivityLine.Operation>().filter { it.type == PendingType.EXPENSE }
        val (cash, digital) = expenses.partition { it.accountKind == AccountKind.CASH }
        return Spending(cash = cash.sumOf { it.amount }, digital = digital.sumOf { it.amount })
    }
}
