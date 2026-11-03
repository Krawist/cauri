// Named after what it declares: the placeholder tabs, with the route of the only one left.
@file:Suppress("MatchingDeclarationName")

package com.cauri

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

// Objectifs comes with phase 3, in its own module (CAU-44). Until then, its tab shows a placeholder declared here.

/** Objectifs tab. */
@Serializable
internal data object GoalsRoute

internal fun NavGraphBuilder.placeholderTabs() {
    composable<GoalsRoute> {
        PlaceholderScreen(title = stringResource(R.string.tab_goals), subtitle = stringResource(R.string.coming_soon))
    }
}
