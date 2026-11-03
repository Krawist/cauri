package com.cauri.core.data.model

/**
 * A prepaid card with its own balance, from [knownPrepaidCards]. It is not declared during the onboarding: Cauri
 * offers to follow it when it recognises its messages.
 *
 * @property code Stable identifier, stored as the account's provider and named by the parsing templates.
 * @property name Name shown to the user and given to the account; brand names are not translated.
 * @property country ISO 3166-1 alpha-2 code of the country where the card is issued, such as `CM`.
 */
data class PrepaidCard(val code: String, val name: String, val country: String)
