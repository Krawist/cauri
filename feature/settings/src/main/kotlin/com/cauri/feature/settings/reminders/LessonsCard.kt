package com.cauri.feature.settings.reminders

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

private val BorderWidth = 1.5.dp
private val LessonIconSize = 18.dp

/**
 * « Ce que Cauri a appris », in gold: each lesson in plain words with its icon, then what it was learnt from and
 * « Repartir de zéro ». While nothing is learnt yet, it says Cauri is still learning.
 */
@Composable
internal fun LessonsCard(state: RemindersUiState, actions: RemindersActions) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderWidth, palette.gold, shape)
            .background(palette.goldSoft, shape)
            .padding(CauriSpacing.l + 2.dp),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sparkle),
                contentDescription = null,
                tint = palette.goldText,
                modifier = Modifier.size(LessonIconSize),
            )
            Text(
                text = stringResource(R.string.reminders_learnt),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.goldText,
            )
        }
        if (state.lessons.isEmpty()) {
            LessonLine(icon = DesignR.drawable.ic_clock, text = stringResource(R.string.reminders_learning))
        }
        state.lessons.forEach { lesson ->
            when (lesson) {
                is Lesson.AnswerHours -> LessonLine(
                    icon = DesignR.drawable.ic_clock,
                    text = stringResource(R.string.reminders_lesson_hours, lesson.from, lesson.until),
                )
                is Lesson.OftenSpends -> LessonLine(
                    icon = DesignR.drawable.ic_place,
                    text = stringResource(R.string.reminders_lesson_often, lesson.place, lesson.timesInTen),
                )
                is Lesson.RarelySpends -> LessonLine(
                    icon = R.drawable.ic_bell_off,
                    text = stringResource(R.string.reminders_lesson_rarely, lesson.place),
                )
            }
        }
        if (state.answers > 0) Footer(state = state, actions = actions)
    }
    if (state.confirmingReset) ResetDialog(actions = actions)
}

@Composable
private fun LessonLine(@DrawableRes icon: Int, text: String) {
    val palette = CauriTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = palette.goldText,
            modifier = Modifier.padding(top = 1.dp).size(LessonIconSize),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = palette.ink)
    }
}

// « Appris sur 214 réponses en 6 semaines » and « Repartir de zéro ».
@Composable
private fun Footer(state: RemindersUiState, actions: RemindersActions) {
    val palette = CauriTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (state.weeks > 0) {
                pluralStringResource(R.plurals.reminders_learnt_from_weeks, state.weeks, state.answers, state.weeks)
            } else {
                pluralStringResource(R.plurals.reminders_learnt_from, state.answers, state.answers)
            },
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { actions.onAskReset(open = true) }) {
            Text(
                text = stringResource(R.string.reminders_reset),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.goldText,
            )
        }
    }
}

@Composable
private fun ResetDialog(actions: RemindersActions) {
    val palette = CauriTheme.palette
    AlertDialog(
        onDismissRequest = { actions.onAskReset(open = false) },
        containerColor = palette.background,
        title = { Text(text = stringResource(R.string.reminders_reset_title), color = palette.ink) },
        text = { Text(text = stringResource(R.string.reminders_reset_text), color = palette.muted) },
        confirmButton = {
            TextButton(onClick = actions::onReset) {
                Text(text = stringResource(R.string.reminders_reset_confirm), color = palette.coralText)
            }
        },
        dismissButton = {
            TextButton(onClick = { actions.onAskReset(open = false) }) {
                Text(text = stringResource(R.string.reminders_reset_cancel), color = palette.ink)
            }
        },
    )
}
