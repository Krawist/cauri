package com.cauri.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.onboarding.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object ProfileRoute

/**
 * Profile, payment habits, categories, accounts and permissions (CAU-06 to CAU-11). Until the next steps exist, the
 * onboarding completes after the profile.
 */
fun NavGraphBuilder.onboardingGraph(onCompleted: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = ProfileRoute) {
        composable<ProfileRoute> { ProfileScreen(onProfileSaved = onCompleted) }
    }
}
