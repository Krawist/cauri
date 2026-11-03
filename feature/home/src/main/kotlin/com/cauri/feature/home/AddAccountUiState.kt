package com.cauri.feature.home

/**
 * State of « Ajouter un compte ».
 *
 * @property query Text typed to search the list.
 * @property choices Mobile money services, then banks, the user does not follow yet and that answer [query].
 * @property chosen Providers of the choices ticked, to add at once.
 * @property added Whether the accounts were just added, so the sheet closes.
 */
internal data class AddAccountUiState(
    val query: String = "",
    val choices: List<AccountChoice> = emptyList(),
    val chosen: Set<String> = emptySet(),
    val added: Boolean = false,
)
