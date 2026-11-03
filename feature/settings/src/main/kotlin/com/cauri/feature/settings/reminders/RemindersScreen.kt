package com.cauri.feature.settings.reminders

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.BackButton
import com.cauri.feature.settings.R
import com.cauri.feature.settings.RowGroup
import com.cauri.feature.settings.SectionLabel
import com.cauri.feature.settings.Separator
import java.time.LocalTime

private val RowHeight = 62.dp

/**
 * « Rappels intelligents »: after visits or not, the time of the evening summary, the quiet hours, the reminders a day
 * and the places without any.
 */
@Composable
internal fun RemindersScreen(
    onBack: () -> Unit,
    onOpenPlaces: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Named apart from the action it backs, which shares its name.
    val back = onBack
    val actions = remember(viewModel, back, onOpenPlaces) {
        object : RemindersActions {
            override fun onBack() = back()

            override fun onVisitReminders(on: Boolean) = viewModel.onVisitReminders(on)

            override fun onSummary(open: Boolean) = viewModel.onSummary(open)

            override fun onSummaryTime(time: LocalTime?) = viewModel.onSummaryTime(time)

            override fun onQuietHours(open: Boolean) = viewModel.onQuietHours(open)

            override fun onQuietFrom(hours: Int) = viewModel.onQuietFrom(hours)

            override fun onQuietUntil(hours: Int) = viewModel.onQuietUntil(hours)

            override fun onDailyCap(step: Int) = viewModel.onDailyCap(step)

            override fun onPlaces() = onOpenPlaces()
        }
    }
    RemindersContent(state = state, actions = actions)
    if (state.choosingQuietHours) QuietHoursSheet(settings = state.settings, actions = actions)
    if (state.choosingSummary) SummaryTimeSheet(state = state, actions = actions)
}

@Composable
private fun RemindersContent(state: RemindersUiState, actions: RemindersActions) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
    ) {
        BackButton(onClick = actions::onBack)
        Text(
            text = stringResource(R.string.reminders_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
            modifier = Modifier.padding(top = CauriSpacing.xl, bottom = CauriSpacing.xxl),
        )
        SectionLabel(stringResource(R.string.reminders_settings))
        SettingsGroup(state = state, actions = actions)
        Text(
            text = stringResource(R.string.reminders_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.padding(start = CauriSpacing.xs, top = CauriSpacing.s, end = CauriSpacing.xs),
        )
    }
}

// After visits, the summary, the quiet hours, the cap and the places without reminder.
@Composable
private fun SettingsGroup(state: RemindersUiState, actions: RemindersActions) {
    val settings = state.settings
    RowGroup {
        VisitRemindersRow(on = settings.visitReminders, onChange = actions::onVisitReminders)
        Separator()
        ReminderRow(
            title = stringResource(R.string.reminders_summary),
            text = stringResource(R.string.reminders_summary_text),
            onClick = { actions.onSummary(open = true) },
        ) {
            Value(
                if (settings.summaryTime == null) {
                    stringResource(
                        R.string.reminders_summary_auto,
                        state.summaryTime.hour,
                        state.summaryTime.minute,
                    )
                } else {
                    stringResource(R.string.reminders_time, state.summaryTime.hour, state.summaryTime.minute)
                },
            )
        }
        Separator()
        ReminderRow(
            title = stringResource(R.string.reminders_quiet_hours),
            text = stringResource(R.string.reminders_quiet_hours_text),
            onClick = { actions.onQuietHours(open = true) },
        ) {
            Value(quietHours(settings))
        }
        Separator()
        ReminderRow(
            title = stringResource(R.string.reminders_daily_cap),
            text = stringResource(R.string.reminders_daily_cap_text),
        ) {
            Stepper(
                value = settings.dailyCap,
                canLower = settings.dailyCap > RemindersViewModel.MIN_CAP,
                canRaise = settings.dailyCap < RemindersViewModel.MAX_CAP,
                onStep = actions::onDailyCap,
            )
        }
        Separator()
        ReminderRow(
            title = stringResource(R.string.reminders_quiet_places),
            text = state.quietPlaces.takeIf { it.isNotEmpty() }?.joinToString()
                ?: stringResource(R.string.reminders_quiet_places_none),
            onClick = actions::onPlaces,
        ) {}
    }
}

// « Rappels après une visite » and its switch.
@Composable
private fun VisitRemindersRow(on: Boolean, onChange: (Boolean) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    ReminderRow(
        title = stringResource(R.string.reminders_after_visits),
        text = stringResource(R.string.reminders_after_visits_text),
    ) {
        Switch(
            checked = on,
            onCheckedChange = {
                haptics.select()
                onChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = palette.accent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = palette.chip,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

// A setting: its title and text, then its value or control on the right, with a chevron when a tap opens more.
@Composable
private fun ReminderRow(
    title: String,
    text: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit,
) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        trailing()
        if (onClick != null) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = null,
                tint = palette.muted,
            )
        }
    }
}

@Composable
private fun Value(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.muted,
    )
}

/** « 22:00 – 07:00 », or « Aucune » when the quiet hours start and end at the same hour. */
@Composable
internal fun quietHours(settings: ReminderSettings): String = if (settings.quietFrom == settings.quietUntil) {
    stringResource(R.string.reminders_quiet_hours_none)
} else {
    stringResource(R.string.reminders_quiet_hours_value, settings.quietFrom, settings.quietUntil)
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RemindersContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            RemindersContent(
                state = RemindersUiState(quietPlaces = listOf("Maison", "Travail")),
                actions = PreviewRemindersActions,
            )
        }
    }
}

/** Actions that do nothing, for previews. */
private object PreviewRemindersActions : RemindersActions {
    override fun onBack() = Unit

    override fun onVisitReminders(on: Boolean) = Unit

    override fun onSummary(open: Boolean) = Unit

    override fun onSummaryTime(time: LocalTime?) = Unit

    override fun onQuietHours(open: Boolean) = Unit

    override fun onQuietFrom(hours: Int) = Unit

    override fun onQuietUntil(hours: Int) = Unit

    override fun onDailyCap(step: Int) = Unit

    override fun onPlaces() = Unit
}
