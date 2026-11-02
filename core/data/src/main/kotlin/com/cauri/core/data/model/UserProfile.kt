package com.cauri.core.data.model

/**
 * The user's profile, saved complete at the end of its onboarding steps and kept on the phone. Every answer about
 * the user lives here.
 *
 * @property displayName display name, as typed, without surrounding spaces.
 * @property occupation What the user does for a living.
 * @property incomeType How their income arrives.
 * @property paymentHabit How they mostly pay, used by the reminder rules.
 */
data class UserProfile(
    val displayName: String,
    val occupation: Occupation,
    val incomeType: IncomeType,
    val paymentHabit: PaymentHabit,
)
