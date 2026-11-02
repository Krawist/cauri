package com.cauri.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

fun NavController.navigateToSettings() = navigate(SettingsGraph)

/** Settings: the status of each permission and of the battery (CAU-10, CAU-11). */
fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> { SettingsScreen(onBack = onBack) }
    }
}
