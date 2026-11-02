package com.cauri.feature.onboarding.categories

/** What the user can do on the category selection screen. */
internal interface CategoriesActions {
    fun onPackCategoryToggle(category: PackCategory)

    /** Removes a category the user added: it is theirs, there is nothing to keep it for. */
    fun onAddedCategoryRemove(name: String)

    fun onAddStart()

    fun onNewNameChange(name: String)

    /** Adds the typed name as a kept category, unless it is blank or already added. */
    fun onAddConfirm()

    /** Gives the chosen categories when at least one is kept, otherwise points it out. */
    fun onContinue()
}
