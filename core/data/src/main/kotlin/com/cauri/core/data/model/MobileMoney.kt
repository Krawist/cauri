package com.cauri.core.data.model

/**
 * A mobile money service the user can declare, from [knownMobileMoney].
 *
 * @property code Stable identifier, stored as the account's provider: the parsing templates use it to read the
 * service's notifications. It never changes, even when the service is renamed.
 * @property name Name shown to the user and given to the account; brand names are not translated.
 * @property description Line shown under the name; empty for now.
 * @property country ISO 3166-1 alpha-2 code of the country where the service operates, such as `CM`.
 */
data class MobileMoney(val code: String, val name: String, val description: String, val country: String)
