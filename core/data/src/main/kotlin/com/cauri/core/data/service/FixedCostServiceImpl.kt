package com.cauri.core.data.service

import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import java.text.Normalizer
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** Default implementation for [FixedCostService]. */
@Singleton
internal class FixedCostServiceImpl @Inject constructor() : FixedCostService {
    override fun suggest(expenses: List<FixedCostSpend>, zone: ZoneId): List<FixedCostSuggestion> = expenses
        .groupBy { key(it) }
        .filterKeys { it != null }
        .values
        .mapNotNull { payments -> suggestion(payments, zone) }

    override fun pays(expense: FixedCostSpend, cost: NewFixedCost): Boolean {
        val name = plain(cost.name)
        val merchant = cost.counterparty?.let(::plain)
        val sameMerchant = merchant != null && expense.counterparty?.let(::plain) == merchant
        val named = listOfNotNull(expense.counterparty, expense.note, expense.categoryName).any { plain(it) == name }
        val sameCategory = cost.categoryId != null &&
            expense.categoryId == cost.categoryId &&
            close(expense.amount, cost.amount)
        return sameMerchant || named || sameCategory
    }

    override fun same(suggested: NewFixedCost, known: NewFixedCost): Boolean {
        val merchant = suggested.counterparty
        return if (merchant != null) {
            known.counterparty?.let(::plain) == plain(merchant)
        } else {
            known.counterparty == null &&
                known.categoryId == suggested.categoryId &&
                close(suggested.amount, known.amount)
        }
    }

    // One payment in each month, in enough months, for about the same amount.
    private fun suggestion(payments: List<FixedCostSpend>, zone: ZoneId): FixedCostSuggestion? {
        val months = payments.groupBy { YearMonth.from(it.occurredAt.atZone(zone)) }
        val amounts = payments.map { it.amount }.sorted()
        val median = amounts[amounts.size / 2]
        val monthly = months.size >= FixedCostService.MIN_MONTHS && months.values.all { it.size == 1 }
        if (!monthly || amounts.any { !close(it, median) }) return null
        val latest = payments.maxBy { it.occurredAt }
        return FixedCostSuggestion(
            cost = NewFixedCost(
                name = latest.counterparty?.trim() ?: latest.categoryName.orEmpty(),
                amount = median,
                dayOfMonth = latest.occurredAt.atZone(zone).dayOfMonth,
                categoryId = latest.categoryId,
                counterparty = latest.counterparty?.trim(),
            ),
            months = months.size,
        )
    }

    // The merchant; without one, the category and the amount: a typed rent comes back with the same amount.
    private fun key(expense: FixedCostSpend): String? = expense.counterparty?.takeIf { it.isNotBlank() }?.let(::plain)
        ?: expense.categoryId?.let { "$it/${expense.amount}" }

    private fun close(amount: Long, other: Long) = abs(amount - other) <= other * TOLERANCE

    // Lower case, without accents nor spaces around.
    private fun plain(text: String): String = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }

    private companion object {
        const val TOLERANCE = 0.1
    }
}
