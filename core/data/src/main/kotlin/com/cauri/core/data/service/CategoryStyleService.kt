package com.cauri.core.data.service

import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature

/** The look Cauri gives a category the user did not dress: an icon and a colour from its name and nature. */
internal interface CategoryStyleService {
    /** The icon of [name], a category of [nature]: from words of its name (accents and case ignored), or its nature. */
    fun icon(name: String, nature: CategoryNature): CategoryIcon

    /** The colour of [icon], as the mockup gives them. */
    fun color(icon: CategoryIcon): CategoryColor
}
