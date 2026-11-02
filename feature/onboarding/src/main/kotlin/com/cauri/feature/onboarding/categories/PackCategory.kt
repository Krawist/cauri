package com.cauri.feature.onboarding.categories

import androidx.annotation.StringRes
import com.cauri.core.data.model.CategoryNature
import com.cauri.feature.onboarding.R

/**
 * A category of the Cameroon pack offered during the onboarding.
 *
 * @property label Name of the category, created in the user's language.
 * @property nature Whether it counts spending or savings.
 * @property keptByDefault Whether it is selected when the screen opens.
 */
internal enum class PackCategory(
    @param:StringRes val label: Int,
    val nature: CategoryNature,
    val keptByDefault: Boolean,
) {
    FOOD(R.string.category_food, CategoryNature.EXPENSE, keptByDefault = true),
    TRANSPORT(R.string.category_transport, CategoryNature.EXPENSE, keptByDefault = true),
    AIRTIME_AND_DATA(R.string.category_airtime_and_data, CategoryNature.EXPENSE, keptByDefault = true),
    FAMILY(R.string.category_family, CategoryNature.EXPENSE, keptByDefault = true),

    /** Counted as savings, never as spending. */
    TONTINE(R.string.category_tontine, CategoryNature.SAVING, keptByDefault = true),
    HOUSING_AND_BILLS(R.string.category_housing_and_bills, CategoryNature.EXPENSE, keptByDefault = true),
    HEALTH(R.string.category_health, CategoryNature.EXPENSE, keptByDefault = false),
    LEISURE(R.string.category_leisure, CategoryNature.EXPENSE, keptByDefault = false),
    EDUCATION(R.string.category_education, CategoryNature.EXPENSE, keptByDefault = false),
    CLOTHING(R.string.category_clothing, CategoryNature.EXPENSE, keptByDefault = false),

    /** The envelope for the family's frequent and unexpected requests: a budget, so spending. */
    FAMILY_EMERGENCIES(R.string.category_family_emergencies, CategoryNature.EXPENSE, keptByDefault = false),
}
