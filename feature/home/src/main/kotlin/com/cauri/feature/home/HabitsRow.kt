package com.cauri.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Habit
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val PlusTileWidth = 52.dp
private val TileGap = CauriSpacing.s

// Three habits fill the row, as in the mockup; more scroll sideways.
private const val VISIBLE_HABITS = 3

/**
 * Row of one-tap cash expenses: a tap records one, a long-press opens it in the habit sheet. « + » at the end opens
 * the quick entry, through [onAddExpense]. Without habits, a tile invites to add the first one.
 */
@Composable
internal fun HabitsRow(habits: List<Habit>, actions: HabitActions, onAddExpense: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tileWidth = (maxWidth - PlusTileWidth - TileGap * VISIBLE_HABITS) / VISIBLE_HABITS
        val addTileWidth = maxWidth - PlusTileWidth - TileGap
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(TileGap),
        ) {
            if (habits.isEmpty()) {
                AddHabitTile(
                    onClick = { actions.onEditHabit(null) },
                    modifier = Modifier.width(addTileWidth),
                )
            }
            habits.forEach { habit ->
                HabitTile(habit = habit, actions = actions, modifier = Modifier.width(tileWidth))
            }
            PlusTile(onClick = onAddExpense)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitTile(habit: Habit, actions: HabitActions, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.control),
        modifier = modifier.fillMaxHeight(),
        interaction = Modifier.combinedClickable(
            role = Role.Button,
            onLongClickLabel = stringResource(R.string.home_habit_edit),
            onLongClick = { actions.onEditHabit(habit) },
            onClick = { actions.onHabitTap(habit) },
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
private fun AddHabitTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.control),
        modifier = modifier.fillMaxHeight(),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(horizontal = CauriSpacing.m, vertical = CauriSpacing.s + 2.dp)) {
            Text(
                text = stringResource(R.string.home_habit_add),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.accentText,
            )
            Text(
                text = stringResource(R.string.home_habit_add_hint),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

@Composable
private fun PlusTile(onClick: () -> Unit) {
    val description = stringResource(R.string.home_other_cash_expense)
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.control),
        modifier = Modifier.width(PlusTileWidth).fillMaxHeight(),
        interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_nav_add),
            contentDescription = description,
            tint = CauriTheme.palette.ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
