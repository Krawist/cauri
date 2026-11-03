package com.cauri.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Habit
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.HelpBubble
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.component.pressScale
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val TileGap = CauriSpacing.s
private val TileHeight = 56.dp
private val AddButtonHeight = CauriSpacing.touchTarget
private val AddIconSize = 18.dp

// Three habits a line, as in the mockup; two lines at most, the others scroll inside them.
private const val HABITS_PER_LINE = 3

// The help bubble of the habits: a tap records, a long press opens the sheet.
private const val HELP_HABITS = "habits"
private const val VISIBLE_LINES = 2

/**
 * One-tap cash expenses, three a line on two lines at most, more scrolling inside them: a tap records one, a
 * long-press opens it in the habit sheet. « Ajouter une habitude », dashed under them and always in reach, opens the
 * sheet of a new habit.
 */
@Composable
internal fun HabitsRow(habits: List<Habit>, actions: HabitActions) {
    Column(verticalArrangement = Arrangement.spacedBy(TileGap)) {
        if (habits.isNotEmpty()) {
            val lines = habits.chunked(HABITS_PER_LINE)
            val visible = minOf(lines.size, VISIBLE_LINES)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TileHeight * visible + TileGap * (visible - 1))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TileGap),
            ) {
                lines.forEach { line ->
                    Row(horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                        line.forEach { habit ->
                            HabitTile(
                                habit = habit,
                                actions = actions,
                                modifier = Modifier.weight(1f).height(TileHeight),
                            )
                        }
                        // A short last line keeps the width of the others.
                        repeat(HABITS_PER_LINE - line.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            HelpBubble(id = HELP_HABITS, text = stringResource(R.string.home_help_habits))
        }
        AddHabitButton(onClick = { actions.onEditHabit(null) })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitTile(habit: Habit, actions: HabitActions, modifier: Modifier = Modifier) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.control),
        // A tap records an expense: the tile sinks under the finger.
        modifier = modifier.fillMaxHeight().pressScale(),
        interaction = Modifier.combinedClickable(
            role = Role.Button,
            onLongClickLabel = stringResource(R.string.home_habit_edit),
            onLongClick = {
                haptics.hold()
                actions.onEditHabit(habit)
            },
            // The expense is recorded at once: the tap feels like a confirmation.
            onClick = {
                haptics.confirm()
                actions.onHabitTap(habit)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.m, vertical = CauriSpacing.s + 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.home_amount, formatFcfa(habit.amount)),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

@Composable
private fun AddHabitButton(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AddButtonHeight)
            .clip(RoundedCornerShape(CauriRadius.control))
            .dashedBorder(palette.handle, radius = CauriRadius.control)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_nav_add),
                contentDescription = null,
                tint = palette.accentText,
                modifier = Modifier.size(AddIconSize),
            )
            Text(
                text = stringResource(R.string.home_habit_add),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.accentText,
            )
        }
    }
}
