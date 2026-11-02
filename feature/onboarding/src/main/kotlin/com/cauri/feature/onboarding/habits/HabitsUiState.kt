package com.cauri.feature.onboarding.habits

import com.cauri.core.data.model.PaymentHabit

/**
 * State of the payment habits screen.
 *
 * @property paymentHabit Chosen way of paying; `null` until the user picks one.
 * @property cashDigits Cash in pocket as typed, digits only, in FCFA; empty until the user types an amount.
 * @property missingFields Answers to point out: empty until the user first tries to continue.
 * @property saved Whether the answers were saved, so the onboarding can go on.
 */
internal data class HabitsUiState(
    val paymentHabit: PaymentHabit? = null,
    val cashDigits: String = "",
    val missingFields: Set<HabitsField> = emptySet(),
    val saved: Boolean = false,
)
