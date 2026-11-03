package com.cauri.feature.onboarding.categories

import androidx.annotation.StringRes
import com.cauri.feature.onboarding.R

/**
 * A first one-tap cash expense, suggested when the user keeps its [category] during the onboarding.
 *
 * @property label Name of the habit, created in the user's language.
 * @property amount Usual amount, in FCFA.
 * @property category Pack category whose expenses it records.
 */
internal enum class PackHabit(@param:StringRes val label: Int, val amount: Long, val category: PackCategory) {
    MOTO(R.string.habit_moto, amount = 500, category = PackCategory.TRANSPORT),
    FRITTERS(R.string.habit_fritters, amount = 300, category = PackCategory.FOOD),
    TAXI(R.string.habit_taxi, amount = 350, category = PackCategory.TRANSPORT),
    AIRTIME(R.string.habit_airtime, amount = 500, category = PackCategory.AIRTIME_AND_DATA),
}
