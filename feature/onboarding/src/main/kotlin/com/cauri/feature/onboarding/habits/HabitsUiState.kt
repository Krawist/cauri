package com.cauri.feature.onboarding.habits

import com.cauri.core.data.model.PaymentHabit

/**
 * State of the payment habits screen.
 *
 * @property paymentHabit Chosen way of paying; `null` until the user picks one.
 * @property cashDigits Cash in pocket as typed, digits only, in FCFA; empty until the user types an amount.
 * @property complete Whether every required answer is given, so « Continuer » can be shown active.
 * @property missingFields Answers to point out: empty until the user first tries to continue.
 * @property answered The answers, once both given and the user continued, for the screen to hand on; `null` otherwise.
 */
internal data class HabitsUiState(
    val paymentHabit: PaymentHabit? = null,
    val cashDigits: String = "",
    val complete: Boolean = false,
    val missingFields: Set<HabitsField> = emptySet(),
    val answered: HabitsAnswers? = null,
)
