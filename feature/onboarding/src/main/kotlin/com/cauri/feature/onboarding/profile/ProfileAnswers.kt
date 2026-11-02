package com.cauri.feature.onboarding.profile

import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation

/**
 * The answers of the profile step, handed on to the payment habits step, which saves the whole profile.
 *
 * @property displayName First name as typed, without surrounding spaces.
 * @property occupation Chosen occupation.
 * @property incomeType Chosen income type.
 */
internal data class ProfileAnswers(val displayName: String, val occupation: Occupation, val incomeType: IncomeType)
