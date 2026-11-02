package com.cauri.feature.onboarding.categories

/**
 * State of the category selection screen.
 *
 * @property keptPack Categories of the pack the user keeps.
 * @property added Names of the categories the user added, all kept, in the order added.
 * @property newName Name being typed for a new category.
 * @property adding Whether the field to add a category is open.
 * @property complete Whether at least one category is kept, so « Continuer » can be shown active.
 * @property noneKept Whether to point out that no category is kept: only after the user tried to continue.
 * @property answered The chosen categories, once at least one is kept and the user continued; `null` otherwise.
 */
internal data class CategoriesUiState(
    val keptPack: Set<PackCategory> = PackCategory.entries.filter { it.keptByDefault }.toSet(),
    val added: List<String> = emptyList(),
    val newName: String = "",
    val adding: Boolean = false,
    val complete: Boolean = true,
    val noneKept: Boolean = false,
    val answered: CategoriesAnswers? = null,
)
