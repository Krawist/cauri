package com.cauri

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

// Objectifs and Analyses come with phase 3, in their own modules (CAU-44, CAU-47). Until then, their tabs show a
// placeholder declared here.

/** Objectifs tab. */
@Serializable
internal data object GoalsRoute

/** Analyses tab. */
@Serializable
internal data object InsightsRoute

internal fun NavGraphBuilder.placeholderTabs() {
    composable<GoalsRoute> {
        PlaceholderScreen(title = stringResource(R.string.tab_goals), subtitle = stringResource(R.string.coming_soon))
    }
    composable<InsightsRoute> {
        PlaceholderScreen(
            title = stringResource(R.string.tab_insights),
            subtitle = stringResource(R.string.coming_soon),
        )
    }
}
