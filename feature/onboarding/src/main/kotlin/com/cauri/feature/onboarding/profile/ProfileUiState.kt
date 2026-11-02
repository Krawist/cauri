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
 * @property answered The answers, once all given and the user continued, for the screen to hand on; `null` otherwise.
 */
internal data class ProfileUiState(
    val firstName: String = "",
    val occupation: Occupation? = null,
    val incomeType: IncomeType? = null,
    val missingFields: Set<ProfileField> = emptySet(),
    val answered: ProfileAnswers? = null,
)
