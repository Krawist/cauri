package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Habit
import com.cauri.core.design.component.ReorderPickUp
import com.cauri.core.design.component.ReorderableColumn
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val RowHeight = 60.dp
private val RowGap = CauriSpacing.s
private val HandleWidth = 52.dp
private val DraggedElevation = 12.dp

/** « Mes habitudes »: the habits in the order of the row, dragged by their handle to reorder them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageHabitsSheet(habits: List<Habit>, actions: HabitActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = { actions.onManageHabits(open = false) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.sheet.compositeOver(palette.background),
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
        ) {
            Text(text = stringResource(R.string.home_habits_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.home_habits_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.padding(top = CauriSpacing.xs, bottom = CauriSpacing.l),
            )
            ReorderableHabits(habits = habits, onReorder = actions::onReorder)
            Button(
                onClick = { actions.onManageHabits(open = false) },
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.xl).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.home_habits_done), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// The habits, each dragged by its handle; the new order is saved when the finger lifts.
@Composable
private fun ReorderableHabits(habits: List<Habit>, onReorder: (List<Long>) -> Unit) {
    ReorderableColumn(
        items = habits,
        key = { it.id },
        step = RowHeight,
        onReorder = { order -> onReorder(order.map { it.id }) },
        pickUp = ReorderPickUp.TOUCH,
        gap = RowGap,
    ) { habit, moving, drag ->
        HabitLine(habit = habit, moving = moving, modifier = Modifier, handle = drag)
    }
}

@Composable
private fun HabitLine(habit: Habit, moving: Boolean, modifier: Modifier, handle: Modifier) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.tile)
    GlassSurface(
        shape = shape,
        // The dragged line is opaque: the line it passes over never shows through it.
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .then(
                if (moving) Modifier.background(palette.sheet.compositeOver(palette.background), shape) else Modifier,
            ),
        strong = moving,
        elevation = if (moving) DraggedElevation else 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                modifier = Modifier.weight(1f).padding(start = CauriSpacing.l),
            )
            Text(
                text = stringResource(R.string.home_amount, formatFcfa(habit.amount)),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Box(
                modifier = handle.width(HandleWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drag),
                    contentDescription = stringResource(R.string.home_habits_drag),
                    tint = if (moving) palette.accentText else palette.muted,
                )
            }
        }
    }
}
