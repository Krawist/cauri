package com.cauri.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val BarHeight = 68.dp
private val TabSize = 48.dp
private val AddButtonSize = 52.dp
private val BarElevation = 16.dp
private val AddButtonElevation = 8.dp

/**
 * Floating glass bottom bar: the [tabs] around a central « + » button. The selected tab is drawn in lagoon.
 *
 * @param tabs The tabs; the « + » button sits before the middle one (between the 2nd and 3rd of 4).
 * @param selectedTab Index of the selected tab in [tabs], or -1 when none is.
 * @param addLabel Name of the « + » button, read by accessibility services.
 */
@Composable
fun CauriBottomBar(
    tabs: List<BottomBarTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    GlassSurface(
        shape = CircleShape,
        strong = true,
        elevation = BarElevation,
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = CauriSpacing.l, end = CauriSpacing.l, bottom = CauriSpacing.xl)
            .fillMaxWidth()
            .height(BarHeight),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = CauriSpacing.s).selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val middle = tabs.size / 2
            tabs.forEachIndexed { index, tab ->
                if (index == middle) {
                    AddButton(label = addLabel, onClick = {
                        haptics.select()
                        onAdd()
                    })
                }
                val selected = index == selectedTab
                Box(
                    modifier = Modifier
                        .size(TabSize)
                        // Round press feedback, like the bar and the « + » button.
                        .clip(CircleShape)
                        .selectable(selected = selected, role = Role.Tab, onClick = {
                            // A tick only when the tab changes.
                            if (!selected) haptics.select()
                            onTabSelected(index)
                        })
                        .semantics { contentDescription = tab.label },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        tint = if (selected) palette.accent else palette.muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddButton(label: String, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(AddButtonSize)
            .shadow(elevation = AddButtonElevation, shape = CircleShape, spotColor = palette.accent)
            .background(palette.accent, CircleShape),
    ) {
        Icon(painter = painterResource(R.drawable.ic_nav_add), contentDescription = label, tint = palette.onAccent)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CauriBottomBarPreview() {
    CauriTheme {
        CauriBottomBar(
            tabs = listOf(
                BottomBarTab(icon = R.drawable.ic_nav_home, label = "Accueil"),
                BottomBarTab(icon = R.drawable.ic_nav_activity, label = "Activité"),
                BottomBarTab(icon = R.drawable.ic_nav_goals, label = "Objectifs"),
                BottomBarTab(icon = R.drawable.ic_nav_insights, label = "Analyses"),
            ),
            selectedTab = 0,
            onTabSelected = {},
            addLabel = "Ajouter une dépense",
            onAdd = {},
        )
    }
}
