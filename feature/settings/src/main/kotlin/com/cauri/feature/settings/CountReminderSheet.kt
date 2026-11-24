package com.cauri.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val HourHeight = 44.dp
private val SelectedBorderWidth = 1.5.dp

// The evening hours the reminder can start from.
private val ReminderHours = listOf(19, 20, 21, 22)

/** « Rappels de comptage »: how often, and from which hour of the evening. Each choice is saved at once. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountReminderSheet(settings: CountReminderSettings, actions: SettingsActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = { actions.onCountReminder(open = false) },
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
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Text(
                text = stringResource(R.string.settings_count_reminder),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.settings_count_reminder_why),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.padding(bottom = CauriSpacing.s),
            )
            CountReminderFrequency.entries.forEach {
                FrequencyChoice(frequency = it, settings = settings, actions = actions)
            }
            if (settings.frequency != CountReminderFrequency.NEVER) HourChoice(settings = settings, actions = actions)
            Button(
                onClick = { actions.onCountReminder(open = false) },
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.l).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.settings_done), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun FrequencyChoice(
    frequency: CountReminderFrequency,
    settings: CountReminderSettings,
    actions: SettingsActions,
) {
    val palette = CauriTheme.palette
    val (title, text) = when (frequency) {
        CountReminderFrequency.WHEN_USEFUL ->
            R.string.settings_count_reminder_when_useful to R.string.settings_count_reminder_when_useful_text
        CountReminderFrequency.EVERY_EVENING ->
            R.string.settings_count_reminder_every_evening to R.string.settings_count_reminder_every_evening_text
        CountReminderFrequency.NEVER ->
            R.string.settings_count_reminder_never to
                R.string.settings_count_reminder_never_text
    }
    val selected = settings.frequency == frequency
    Choice(
        selected = selected,
        shape = RoundedCornerShape(CauriRadius.tile),
        onSelect = { actions.onCountReminderChange(settings.copy(frequency = frequency)) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) palette.accentText else palette.ink,
            )
            Text(text = stringResource(text), style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
    }
}

@Composable
private fun HourChoice(settings: CountReminderSettings, actions: SettingsActions) {
    val palette = CauriTheme.palette
    Text(
        text = stringResource(R.string.settings_count_reminder_hour),
        style = MaterialTheme.typography.labelMedium,
        color = palette.muted,
        modifier = Modifier.padding(top = CauriSpacing.m),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        ReminderHours.forEach { hour ->
            val selected = settings.hour == hour
            Choice(
                selected = selected,
                shape = CircleShape,
                onSelect = { actions.onCountReminderChange(settings.copy(hour = hour)) },
                modifier = Modifier.weight(1f).height(HourHeight),
            ) {
                Text(
                    text = stringResource(R.string.settings_count_reminder_hour_value, hour),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) palette.accentText else palette.ink,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

// A glass choice, lagoon once selected, as the answers of the onboarding.
@Composable
private fun Choice(
    selected: Boolean,
    shape: Shape,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = shape,
        modifier = modifier.then(
            if (selected) Modifier.border(SelectedBorderWidth, palette.accent, shape) else Modifier,
        ),
        interaction = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        if (selected) Box(Modifier.matchParentSize().background(palette.accentSoft))
        content()
    }
}
