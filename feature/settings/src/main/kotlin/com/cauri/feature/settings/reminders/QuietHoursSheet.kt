package com.cauri.feature.settings.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.design.component.Stepper
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

private val ButtonHeight = 56.dp

/** « Heures de silence »: from which hour to which hour no reminder comes, an hour a step. Saved at once. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuietHoursSheet(settings: ReminderSettings, actions: RemindersActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = { actions.onQuietHours(open = false) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // The sheet is its own window: there is nothing of the screen to blur, so the sheet colour is opaque.
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
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Text(text = stringResource(R.string.reminders_quiet_hours), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.reminders_quiet_hours_why),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            HourRow(
                title = stringResource(R.string.reminders_quiet_from),
                hour = settings.quietFrom,
                onStep = actions::onQuietFrom,
            )
            HourRow(
                title = stringResource(R.string.reminders_quiet_until),
                hour = settings.quietUntil,
                onStep = actions::onQuietUntil,
            )
            Text(
                text = quietHours(settings),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Button(
                onClick = { actions.onQuietHours(open = false) },
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.settings_done), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// « Début  − 22:00 + »: the hours go round the clock.
@Composable
private fun HourRow(title: String, hour: Int, onStep: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = CauriTheme.palette.ink,
            modifier = Modifier.weight(1f),
        )
        Stepper(
            value = hour,
            canLower = true,
            canRaise = true,
            onStep = onStep,
            label = stringResource(R.string.reminders_hour, hour),
        )
    }
}
