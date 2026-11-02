package com.cauri.feature.onboarding

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

/** Profile, payment habits, categories, accounts and permissions (CAU-06 to CAU-11). */
fun NavGraphBuilder.onboardingGraph(onCompleted: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.onboarding_title),
                subtitle = stringResource(R.string.onboarding_subtitle),
                actionLabel = stringResource(R.string.onboarding_start),
                onAction = onCompleted,
            )
        }
    }
}
