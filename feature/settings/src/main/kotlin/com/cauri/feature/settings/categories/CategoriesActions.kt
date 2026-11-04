package com.cauri.feature.settings.categories

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature

/** What the user can do on « Catégories » and in the sheet of a category. */
// One function per gesture of the screen and of its sheet.
@Suppress("TooManyFunctions")
internal interface CategoriesActions {
    fun onNature(nature: CategoryNature)

    /** Opens or closes the sub-categories of the category [id]. */
    fun onToggle(id: Long)

    /** Opens the sheet of [category], or of a new top-level category of the tab when `null`. */
    fun onEdit(category: Category?)

    /** Opens the sheet of a new sub-category of [parent]. */
    fun onNewSub(parent: Category)

    fun onDraftChange(draft: CategoryDraft)

    fun onDraftSave()

    fun onAddSub()

    fun onRemoveSub(id: Long)

    /** Puts the categories in the order of [ids]. */
    fun onReorder(ids: List<Long>)

    fun onAskMerge(open: Boolean)

    fun onMerge(into: Long)

    fun onArchive()

    fun onShowArchived(open: Boolean)

    fun onRestore(id: Long)

    fun onEditClose()
}
