package com.cauri.feature.onboarding.profile

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation

/**
 * State of the profile screen.
 *
 * @property firstName First name as typed so far.
 * @property occupation Chosen occupation; `null` until the user picks one.
 * @property incomeType Chosen income type; `null` until the user picks one.
 * @property missingFields Answers to point out: empty until the user first tries to continue.
 * @property saved Whether the profile was saved, so the onboarding can go on.
 */
internal data class ProfileUiState(
    val firstName: String = "",
    val occupation: Occupation? = null,
    val incomeType: IncomeType? = null,
    val missingFields: Set<ProfileField> = emptySet(),
    val saved: Boolean = false,
)
