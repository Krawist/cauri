package com.cauri.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.settings.reminders.RemindersScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

@Serializable
internal data object RemindersRoute

fun NavController.navigateToSettings() = navigate(SettingsGraph)

/**
 * Settings: the status of each permission and of the battery (CAU-10, CAU-11), the way to « Mes lieux », and the smart
 * reminders (CAU-38). [onBack] leaves the settings.
 */
fun NavGraphBuilder.settingsGraph(navController: NavController, onBack: () -> Unit, onOpenPlaces: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = onBack,
                onOpenPlaces = onOpenPlaces,
                onOpenReminders = { navController.navigate(RemindersRoute) },
            )
        }
        composable<RemindersRoute> {
            RemindersScreen(onBack = { navController.popBackStack() }, onOpenPlaces = onOpenPlaces)
        }
    }
}
