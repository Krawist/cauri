package com.cauri.core.data.service

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.Spending
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ActivityQueryService]. */
@Singleton
internal class ActivityQueryServiceImpl @Inject constructor() : ActivityQueryService {
    override fun matches(line: ActivityLine, query: String): Boolean {
        val text = plain(query)
        if (text.isEmpty()) return true
        val digits = text.filterNot { it.isWhitespace() }
        val amount = when (line) {
            is ActivityLine.Operation -> line.amount
            is ActivityLine.CashCount -> line.amount
        }
        val words = when (line) {
            is ActivityLine.Operation ->
                listOfNotNull(line.counterparty, line.note, line.categoryName, line.accountName, line.toAccountName)
            is ActivityLine.CashCount -> emptyList()
        }
        return if (digits.all(Char::isDigit)) {
            amount.toString().contains(digits) || words.any { plain(it).contains(text) }
        } else {
            words.any { plain(it).contains(text) }
        }
    }

    override fun days(period: ActivityPeriod, today: LocalDate): ClosedRange<LocalDate>? = when (period) {
        ActivityPeriod.All -> null
        ActivityPeriod.ThisWeek -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))..today
        ActivityPeriod.ThisMonth -> today.withDayOfMonth(1)..today
        ActivityPeriod.LastMonth -> today.minusMonths(1).let {
            it.withDayOfMonth(1)..it.withDayOfMonth(it.lengthOfMonth())
        }
        is ActivityPeriod.Between -> minOf(period.from, period.to)..maxOf(period.from, period.to)
    }

    override fun spending(lines: List<ActivityLine>): Spending {
        val spent = lines.filterIsInstance<ActivityLine.Operation>().filter { it.status != ActivityStatus.SET_ASIDE }

        // A transfer is never spending, its fees are.
        fun ActivityLine.Operation.spending() = when (type) {
            PendingType.EXPENSE -> amount
            PendingType.TRANSFER -> fees
            PendingType.INCOME -> 0
        }
        val (cash, digital) = spent.partition { it.accountKind == AccountKind.CASH }
        return Spending(cash = cash.sumOf { it.spending() }, digital = digital.sumOf { it.spending() })
    }

    // Lower case, without accents nor the spaces that group the digits of amounts.
    private fun plain(text: String): String = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
        .replace(GROUP_SPACES, "")

    private companion object {
        // The no-break and narrow no-break spaces of « 5 000 », and an ordinary space between digits.
        val GROUP_SPACES = Regex("(?<=\\d)[\\s\\u00A0\\u202F](?=\\d)")
    }
}
