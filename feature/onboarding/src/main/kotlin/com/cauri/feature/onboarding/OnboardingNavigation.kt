package com.cauri.feature.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.feature.onboarding.habits.HabitsScreen
import com.cauri.feature.onboarding.profile.ProfileAnswers
import com.cauri.feature.onboarding.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object ProfileRoute

/** Payment habits step, with the profile answers it saves together with its own. */
@Serializable
internal data class HabitsRoute(val displayName: String, val occupation: Occupation, val incomeType: IncomeType)

/**
 * Profile, payment habits, categories, accounts and permissions (CAU-06 to CAU-11). Until the next steps exist, the
 * onboarding completes after the payment habits.
 */
fun NavGraphBuilder.onboardingGraph(navController: NavController, onCompleted: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = ProfileRoute) {
        composable<ProfileRoute> {
            ProfileScreen(
                onProfileAnswered = { answers ->
                    navController.navigate(
                        HabitsRoute(
                            displayName = answers.displayName,
                            occupation = answers.occupation,
                            incomeType = answers.incomeType,
                        ),
                    )
                },
            )
        }
        composable<HabitsRoute> { entry ->
            val route = entry.toRoute<HabitsRoute>()
            HabitsScreen(
                profile = ProfileAnswers(
                    displayName = route.displayName,
                    occupation = route.occupation,
                    incomeType = route.incomeType,
                ),
                onHabitsSaved = onCompleted,
            )
        }
    }
}
