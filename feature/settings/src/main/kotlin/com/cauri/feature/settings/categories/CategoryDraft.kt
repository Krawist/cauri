package com.cauri.feature.settings.categories

import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature

/**
 * A category being created or edited in its sheet.
 *
 * @property id Category edited; `null` for a new one.
 * @property parentId Category it is a sub-category of; `null` for a top-level one.
 * @property nature Spending, income or savings: the tab it was opened from, or its parent's.
 * @property name Name as typed.
 * @property icon Icon chosen.
 * @property color Colour chosen.
 * @property newSub Name of the sub-category being typed; empty when none.
 * @property nameMissing Whether the user tried to save without a name.
 */
internal data class CategoryDraft(
    val id: Long? = null,
    val parentId: Long? = null,
    val nature: CategoryNature = CategoryNature.EXPENSE,
    val name: String = "",
    val icon: CategoryIcon = CategoryIcon.TAG,
    val color: CategoryColor = CategoryColor.LAGOON,
    val newSub: String = "",
    val nameMissing: Boolean = false,
)
