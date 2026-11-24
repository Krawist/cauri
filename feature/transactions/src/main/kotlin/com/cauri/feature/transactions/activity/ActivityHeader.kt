package com.cauri.feature.transactions.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

/**
 * « Activité » and the search button, as in the mockup; once the search is open, a field in its place, the keyboard
 * up, and a cross that closes it.
 */
@Composable
internal fun ActivityHeader(query: String?, actions: ActivityActions) {
    if (query == null) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.transactions_activity_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = CauriTheme.palette.ink,
                modifier = Modifier.weight(1f),
            )
            GlassSurface(
                shape = CircleShape,
                modifier = Modifier.size(CauriSpacing.touchTarget),
                interaction = Modifier.clickable(role = Role.Button, onClick = { actions.onSearch(open = true) }),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.transactions_activity_search),
                    tint = CauriTheme.palette.ink,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    } else {
        SearchField(query = query, actions = actions)
    }
}

@Composable
private fun SearchField(query: String, actions: ActivityActions) {
    val palette = CauriTheme.palette
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    GlassSurface(
        shape = CircleShape,
        strong = true,
        modifier = Modifier.fillMaxWidth().height(CauriSpacing.touchTarget),
    ) {
        Row(
            modifier = Modifier.padding(start = CauriSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(R.drawable.ic_search), contentDescription = null, tint = palette.muted)
            val style = MaterialTheme.typography.bodyLarge.copy(color = palette.ink)
            BasicTextField(
                value = query,
                onValueChange = actions::onQuery,
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.weight(1f).focusRequester(focus),
                decorationBox = { field ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.transactions_activity_search_hint),
                                style = style.copy(color = palette.muted),
                                maxLines = 1,
                            )
                        }
                        field()
                    }
                },
            )
            IconButton(onClick = { actions.onSearch(open = false) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.transactions_activity_search_close),
                    tint = palette.muted,
                )
            }
        }
    }
}
