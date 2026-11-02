package com.cauri.feature.onboarding.habits

import com.cauri.core.data.model.PaymentHabit

/**
 * The answers of the payment habits step, handed on to the onboarding, which saves them at its end.
 *
 * @property paymentHabit Chosen way of paying.
 * @property cashAmount Cash in pocket, in FCFA.
 */
internal data class HabitsAnswers(val paymentHabit: PaymentHabit, val cashAmount: Long)
