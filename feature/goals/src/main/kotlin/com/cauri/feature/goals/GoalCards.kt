package com.cauri.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Goal
import com.cauri.core.data.model.GoalProgress
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val BarHeight = 8.dp
private val IconBoxSize = 44.dp
private val IconBoxRadius = 15.dp
private const val PERCENT = 100

/**
 * A saving: its name and share reached, its gold bar, what it holds out of its amount and when it should be reached;
 * gold all over while the savings go to it.
 */
@Composable
internal fun SavingCard(progress: GoalProgress, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val goal = progress.goal
    val share = (progress.saved * PERCENT / goal.amount).coerceIn(0, PERCENT.toLong()).toInt()
    GoalSurface(onClick = onClick, tint = if (progress.receiving) palette.goldSoft else null) {
        TitleRow(
            name = goal.name,
            end = stringResource(R.string.goals_share, share),
            endColor = palette.goldText,
        )
        Bar(fraction = share / PERCENT.toFloat(), color = palette.gold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Muted(stringResource(R.string.goals_saved, formatFcfa(progress.saved), formatFcfa(goal.amount)))
            Muted(estimate(progress = progress))
        }
    }
}

/** A tontine: its two people, its name, its contribution a month and the day of its payout. */
@Composable
internal fun TontineCard(goal: Goal, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.card),
        modifier = Modifier.fillMaxWidth(),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(CauriSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(IconBoxSize).background(palette.goldSoft, RoundedCornerShape(IconBoxRadius)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_people),
                    contentDescription = null,
                    tint = palette.goldText,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Name(goal.name)
                val contribution = stringResource(R.string.goals_tontine_contribution, formatFcfa(goal.amount))
                Muted(
                    goal.payoutDate?.let {
                        stringResource(
                            R.string.goals_tontine_payout,
                            contribution,
                            it.format(DateTimeFormatter.ofPattern("d MMM", locale)),
                        )
                    } ?: contribution,
                )
            }
        }
    }
}

/** An envelope: its name and what is left, its lagoon bar, and what was used out of its month. */
@Composable
internal fun EnvelopeCard(progress: GoalProgress, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val goal = progress.goal
    val left = goal.amount - progress.saved
    GoalSurface(onClick = onClick, tint = null) {
        TitleRow(
            name = goal.name,
            end = if (left >= 0) {
                stringResource(R.string.goals_envelope_left, formatFcfa(left))
            } else {
                stringResource(R.string.goals_envelope_over, formatFcfa(-left))
            },
            endColor = if (left >= 0) palette.muted else palette.coralText,
        )
        Bar(
            fraction = (progress.saved.toFloat() / goal.amount).coerceIn(0f, 1f),
            color = if (left >= 0) palette.accent else palette.coral,
        )
        Muted(stringResource(R.string.goals_envelope_used, formatFcfa(progress.saved), formatFcfa(goal.amount)))
    }
}

// « vers juin 2027 », « Atteint », or « Date estimée après un mois de revenus ».
@Composable
private fun estimate(progress: GoalProgress): String {
    val estimate: YearMonth? = progress.estimate
    return when {
        progress.saved >= progress.goal.amount -> stringResource(R.string.goals_reached)
        estimate == null -> stringResource(R.string.goals_estimate_later)
        else -> {
            val deadline = progress.goal.deadline
            val late = deadline != null && estimate > YearMonth.from(deadline)
            stringResource(if (late) R.string.goals_estimate_late else R.string.goals_estimate, monthName(estimate))
        }
    }
}

@Composable
private fun GoalSurface(onClick: () -> Unit, tint: Color?, content: @Composable () -> Unit) {
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.card),
        modifier = Modifier.fillMaxWidth(),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        if (tint != null) Box(Modifier.matchParentSize().background(tint))
        Column(
            modifier = Modifier.padding(CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        ) { content() }
    }
}

@Composable
private fun TitleRow(name: String, end: String, endColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) { Name(name) }
        Text(
            text = end,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = endColor,
        )
    }
}

@Composable
private fun Name(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.ink,
    )
}

@Composable
private fun Muted(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = CauriTheme.palette.muted)
}

@Composable
private fun Bar(fraction: Float, color: Color) {
    val shape = RoundedCornerShape(BarHeight / 2)
    Box(modifier = Modifier.fillMaxWidth().height(BarHeight).clip(shape).background(CauriTheme.palette.track)) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(shape).background(color))
    }
}
