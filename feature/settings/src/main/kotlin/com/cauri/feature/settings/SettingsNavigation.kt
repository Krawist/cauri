package com.cauri.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.settings.backup.BackupScreen
import com.cauri.feature.settings.categories.CategoriesScreen
import com.cauri.feature.settings.income.IncomeScreen
import com.cauri.feature.settings.reminders.RemindersScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

@Serializable
internal data object RemindersRoute

@Serializable
internal data object IncomeRoute

@Serializable
internal data object CategoriesRoute

/**
 * « Sauvegarde chiffrée ».
 *
 * @property restoreOnly Whether only the restore is offered, as from the onboarding of a new phone.
 */
@Serializable
internal data class BackupRoute(val restoreOnly: Boolean = false)

fun NavController.navigateToSettings() = navigate(SettingsGraph)

/** Opens the restore of a backup alone, as on a new phone (CAU-48). */
fun NavController.navigateToRestore() = navigate(BackupRoute(restoreOnly = true))

/**
 * Settings: the status of each permission and of the battery (CAU-10, CAU-11), the way to « Mes lieux », and the smart
 * reminders (CAU-38), the split of the incomes (CAU-43), the categories (CAU-56) and
 * the encrypted backup (CAU-48). [onBack] leaves the settings.
 */
fun NavGraphBuilder.settingsGraph(navController: NavController, onBack: () -> Unit, onOpenPlaces: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = onBack,
                onOpenPlaces = onOpenPlaces,
                onOpenReminders = { navController.navigate(RemindersRoute) },
                onOpenIncome = { navController.navigate(IncomeRoute) },
                onOpenCategories = { navController.navigate(CategoriesRoute) },
                onOpenBackup = { navController.navigate(BackupRoute()) },
            )
        }
        composable<RemindersRoute> {
            RemindersScreen(onBack = { navController.popBackStack() }, onOpenPlaces = onOpenPlaces)
        }
        composable<BackupRoute> {
            BackupScreen(onBack = { navController.popBackStack() })
        }
        composable<CategoriesRoute> {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable<IncomeRoute> {
            IncomeScreen(onBack = { navController.popBackStack() })
        }
    }
}
