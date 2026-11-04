package com.cauri.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

// Profile, payment habits, categories, accounts and permissions.
private const val ONBOARDING_STEPS = 6

private val StepWidth = 22.dp
private val StepHeight = 6.dp
private val ButtonHeight = 56.dp
private val SelectedBorderWidth = 1.5.dp

/**
 * Layout of an onboarding step: progress, [title], the scrolling [content], an optional [note] and « Continuer ».
 * Transparent: the app draws the backdrop behind it.
 *
 * @param step Number of this step, from 1.
 * @param complete Whether every required answer is given. « Continuer » is gray until then, but stays enabled: a tap
 * points out what is missing.
 * @param subtitle Line under the title; `null` when there is none.
 * @param continueLabel Text of the button, « Continuer » by default.
 * @param onLater What « Plus tard », under the button, does; `null` for a step that cannot be put off.
 */
@Composable
internal fun OnboardingStep(
    step: Int,
    title: String,
    onContinue: () -> Unit,
    complete: Boolean,
    subtitle: String? = null,
    continueLabel: String = stringResource(R.string.onboarding_continue),
    note: @Composable (() -> Unit)? = null,
    onLater: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = CauriTheme.palette
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = CauriSpacing.xl)) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = CauriSpacing.xl)) {
            StepIndicator(current = step)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = palette.ink,
                modifier = Modifier.padding(
                    top = CauriSpacing.xxl,
                    bottom = if (subtitle ==
                        null
                    ) {
                        CauriSpacing.xl
                    } else {
                        CauriSpacing.s
                    },
                ),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                    modifier = Modifier.padding(bottom = CauriSpacing.xxl),
                )
            }
            content()
            Spacer(Modifier.height(CauriSpacing.xl))
        }
        note?.invoke()
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (complete) palette.accent else palette.trackStrong,
                contentColor = if (complete) palette.onAccent else palette.muted,
            ),
        ) {
            Text(text = continueLabel, style = MaterialTheme.typography.titleMedium)
        }
        if (onLater != null) {
            TextButton(
                onClick = onLater,
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.xs).height(CauriSpacing.touchTarget),
                colors = ButtonDefaults.textButtonColors(contentColor = palette.muted),
            ) {
                Text(text = stringResource(R.string.onboarding_later), style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(CauriSpacing.l))
    }
}

@Composable
private fun StepIndicator(current: Int) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.onboarding_step, current, ONBOARDING_STEPS)
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        repeat(ONBOARDING_STEPS) { index ->
            Box(
                Modifier
                    .size(width = StepWidth, height = StepHeight)
                    .background(if (index < current) palette.accent else palette.trackStrong, CircleShape),
            )
        }
    }
}

/** « Tout reste sur ton téléphone », with a padlock, above the button of the steps that touch personal data. */
@Composable
internal fun LocalOnlyNote() {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = CauriSpacing.m + 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(DesignR.drawable.ic_lock), contentDescription = null, tint = palette.muted)
        Spacer(Modifier.width(CauriSpacing.xs + 2.dp))
        Text(
            text = stringResource(R.string.onboarding_local_only),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}

/** Question above an answer. */
@Composable
internal fun QuestionLabel(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelMedium,
        color = CauriTheme.palette.muted,
        modifier = modifier.padding(bottom = CauriSpacing.s),
    )
}

/** Points out an answer still missing after the user tried to continue. */
@Composable
internal fun MissingAnswer(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodySmall,
        color = CauriTheme.palette.coralText,
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
}

/**
 * A glass answer to pick; the selected one gets a fill and a border, lagoon by default as in the mockups.
 *
 * @param role [Role.RadioButton] for one answer among several, [Role.Checkbox] when several can be picked.
 */
@Composable
internal fun ChoiceSurface(
    selected: Boolean,
    onSelect: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    selectedColor: Color = CauriTheme.palette.accent,
    selectedFill: Color = CauriTheme.palette.accentSoft,
    role: Role = Role.RadioButton,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassSurface(
        shape = shape,
        modifier = modifier.then(
            if (selected) Modifier.border(SelectedBorderWidth, selectedColor, shape) else Modifier,
        ),
        interaction = Modifier.selectable(selected = selected, role = role, onClick = onSelect),
    ) {
        if (selected) Box(Modifier.matchParentSize().background(selectedFill))
        content()
    }
}
