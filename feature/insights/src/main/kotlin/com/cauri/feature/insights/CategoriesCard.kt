package com.cauri.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.CategorySpending
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val BarHeight = 10.dp
private val TickWidth = 2.dp
private val TickHeight = 16.dp

// The longest bar leaves a little room: the budget mark of the costliest stays inside.
private const val SCALE_MARGIN = 1.1f

/**
 * « Dépenses par catégorie »: each category spent on or budgeted, what it cost « / » its budget, one bar on a common
 * scale with the budget marked; in coral once over it.
 */
@Composable
internal fun CategoriesCard(categories: List<CategorySpending>) {
    val palette = CauriTheme.palette
    val scale = categories.maxOf { maxOf(it.spent, it.budget ?: 0) } * SCALE_MARGIN
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.l + 2.dp),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.insights_categories),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(
                        width = TickWidth,
                        height = BarHeight + 2.dp,
                    ).background(palette.ink, RoundedCornerShape(1.dp)),
                )
                Text(
                    text = stringResource(R.string.insights_budget),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    modifier = Modifier.padding(start = CauriSpacing.xs + 2.dp),
                )
            }
            categories.forEach { CategoryLine(category = it, scale = scale) }
        }
    }
}

@Composable
private fun CategoryLine(category: CategorySpending, scale: Float) {
    val palette = CauriTheme.palette
    val budget = category.budget
    val over = budget != null && category.spent > budget
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp)) {
        Row {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = if (over) palette.coralText else palette.ink,
                        ),
                    ) {
                        append(formatFcfa(category.spent))
                    }
                    if (budget != null) {
                        withStyle(SpanStyle(color = palette.muted)) {
                            append(stringResource(R.string.insights_of_budget, formatFcfa(budget)))
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(TickHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(BarHeight).background(palette.track, RoundedCornerShape(BarHeight / 2)))
            Box(
                Modifier
                    .width(maxWidth * (category.spent / scale))
                    .height(BarHeight)
                    .background(if (over) palette.coral else palette.accent, RoundedCornerShape(BarHeight / 2)),
            )
            if (budget != null) {
                Box(
                    Modifier
                        .offset(x = maxWidth * (budget / scale))
                        .size(width = TickWidth, height = TickHeight)
                        .background(palette.ink, RoundedCornerShape(1.dp)),
                )
            }
        }
    }
}
