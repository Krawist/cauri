package com.cauri.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

// Profile, payment habits, categories, accounts and permissions.
private const val ONBOARDING_STEPS = 5

private val StepWidth = 22.dp
private val StepHeight = 6.dp
private val ButtonHeight = 56.dp
private val FieldHeight = 56.dp
private val SelectedBorderWidth = 1.5.dp

/**
 * Layout of an onboarding step: progress, [title], the scrolling [content], an optional [note] and « Continuer »,
 * always enabled. Transparent: the app draws the backdrop behind it.
 *
 * @param step Number of this step, from 1.
 */
@Composable
internal fun OnboardingStep(
    step: Int,
    title: String,
    onContinue: () -> Unit,
    note: @Composable (() -> Unit)? = null,
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
                modifier = Modifier.padding(top = CauriSpacing.xxl, bottom = CauriSpacing.xl),
            )
            content()
            Spacer(Modifier.height(CauriSpacing.xl))
        }
        note?.invoke()
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(text = stringResource(R.string.onboarding_continue), style = MaterialTheme.typography.titleMedium)
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

/** Single-line field: lagoon border while focused, coral when its answer is [missing]. */
@Composable
internal fun OnboardingField(
    value: String,
    onValueChange: (String) -> Unit,
    missing: Boolean,
    textStyle: TextStyle,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    height: Dp = FieldHeight,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.control)
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        missing -> palette.coralText
        focused -> palette.accent
        else -> palette.glassBorder
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle.copy(color = palette.ink),
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .background(palette.field, shape)
            .border(SelectedBorderWidth, borderColor, shape),
        decorationBox = { field ->
            Row(
                modifier = Modifier.padding(horizontal = CauriSpacing.l + 2.dp),
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // With a suffix (« 7 500 F »), the field is as wide as its text, so the suffix follows it.
                val fieldWidth = if (trailing == null) Modifier.weight(1f) else Modifier.width(IntrinsicSize.Max)
                Box(modifier = fieldWidth) { field() }
                trailing?.invoke(this)
            }
        },
    )
}

/** A glass answer to pick; the selected one gets a lagoon fill and border, as in the mockups. */
@Composable
internal fun ChoiceSurface(
    selected: Boolean,
    onSelect: () -> Unit,
    shape: Shape,
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
