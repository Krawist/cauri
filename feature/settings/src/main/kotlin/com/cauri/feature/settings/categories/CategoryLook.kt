package com.cauri.feature.settings.categories

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

private val IconBoxRadius = 14.dp

/** The drawable of [icon]. */
@DrawableRes
internal fun iconOf(icon: CategoryIcon): Int = when (icon) {
    CategoryIcon.FOOD -> R.drawable.ic_category_food
    CategoryIcon.TRANSPORT -> R.drawable.ic_category_transport
    CategoryIcon.HOUSE -> R.drawable.ic_category_house
    CategoryIcon.PHONE -> R.drawable.ic_category_phone
    CategoryIcon.FAMILY -> R.drawable.ic_category_family
    CategoryIcon.EDUCATION -> R.drawable.ic_category_education
    CategoryIcon.LEISURE -> R.drawable.ic_category_leisure
    CategoryIcon.HEALTH -> R.drawable.ic_category_health
    CategoryIcon.SAVING -> R.drawable.ic_category_saving
    CategoryIcon.INCOME -> R.drawable.ic_category_income
    CategoryIcon.TAG -> R.drawable.ic_category_tag
}

/** The palette's shade of [color]: the icon's, and the soft one behind it. */
@Composable
internal fun shadesOf(color: CategoryColor): Pair<Color, Color> {
    val palette = CauriTheme.palette
    return when (color) {
        CategoryColor.LAGOON -> palette.accentText to palette.accentSoft
        CategoryColor.GOLD -> palette.goldText to palette.goldSoft
        CategoryColor.CORAL -> palette.coralText to palette.coralSoft
        CategoryColor.GREEN -> palette.greenText to palette.greenSoft
        CategoryColor.NEUTRAL -> palette.ink to palette.chip
    }
}

/** The swatch of [color] in the sheet's picker: the colour itself. */
@Composable
internal fun swatchOf(color: CategoryColor): Color {
    val palette = CauriTheme.palette
    return when (color) {
        CategoryColor.LAGOON -> palette.accent
        CategoryColor.GOLD -> palette.gold
        CategoryColor.CORAL -> palette.coral
        CategoryColor.GREEN -> palette.greenText
        CategoryColor.NEUTRAL -> palette.muted
    }
}

/** [icon] on the soft shade of [color], in a rounded square of [size]. */
@Composable
internal fun CategoryBadge(icon: CategoryIcon, color: CategoryColor, size: Dp = 40.dp) {
    val (tint, background) = shadesOf(color)
    Box(
        modifier = Modifier.size(size).background(background, RoundedCornerShape(IconBoxRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(iconOf(icon)), contentDescription = null, tint = tint)
    }
}

/** The badge of [category]. */
@Composable
internal fun CategoryBadge(category: Category) = CategoryBadge(icon = category.icon, color = category.color)
