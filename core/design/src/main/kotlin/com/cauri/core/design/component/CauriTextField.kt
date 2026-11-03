package com.cauri.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val FieldHeight = 56.dp
private val BorderWidth = 1.5.dp

/**
 * Single-line text field of the forms: lagoon border while focused, coral when its answer is [missing].
 *
 * @param trailing Shown right after the text, such as the currency of an amount.
 */
@Composable
fun CauriTextField(
    value: String,
    onValueChange: (String) -> Unit,
    missing: Boolean,
    textStyle: TextStyle,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    height: Dp = FieldHeight,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String? = null,
    autoFocus: Boolean = false,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.control)
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }
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
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .background(palette.field, shape)
            .border(BorderWidth, borderColor, shape),
        decorationBox = { field ->
            Row(
                modifier = Modifier.padding(horizontal = CauriSpacing.l + 2.dp),
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // With a suffix (« 7 500 F »), the field is as wide as its text, so the suffix follows it.
                val fieldWidth = if (trailing == null) Modifier.weight(1f) else Modifier.width(IntrinsicSize.Max)
                Box(modifier = fieldWidth) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(text = placeholder, style = textStyle, color = palette.muted)
                    }
                    field()
                }
                trailing?.invoke(this)
            }
        },
    )
}
