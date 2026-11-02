package com.cauri.core.data.model

/**
 * The user's profile, filled in during the onboarding and kept on the phone.
 *
 * @property displayName display name, as typed, without surrounding spaces.
 * @property occupation What the user does for a living.
 * @property incomeType How their income arrives.
 */
data class UserProfile(val displayName: String, val occupation: Occupation, val incomeType: IncomeType)
