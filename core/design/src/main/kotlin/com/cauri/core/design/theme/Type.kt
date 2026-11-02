package com.cauri.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.cauri.core.design.R

// Inter variable font (OFL, licence in licenses/Inter-OFL.txt): one file, one instance per weight used.
@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = Font(
    resId = R.font.inter,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val CauriFontFamily = FontFamily(
    inter(FontWeight.Normal),
    inter(FontWeight.Medium),
    inter(FontWeight.SemiBold),
    inter(FontWeight.Bold),
)

// Tabular figures everywhere, as in the mockups: amounts line up in lists and do not jump when they change.
private fun cauriStyle(size: Int, weight: FontWeight, letterSpacing: TextUnit = 0.em) = TextStyle(
    fontFamily = CauriFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    letterSpacing = letterSpacing,
    fontFeatureSettings = "tnum",
)

/**
 * Type scale of the mockups. Display styles are for amounts (the cash on hand, a counted total), headlines for screen
 * titles, titles for section headers and row titles, labels for buttons and chips.
 */
val CauriTypography = Typography(
    displayLarge = cauriStyle(size = 46, weight = FontWeight.Bold, letterSpacing = (-0.035).em),
    displaySmall = cauriStyle(size = 32, weight = FontWeight.Bold, letterSpacing = (-0.03).em),
    headlineLarge = cauriStyle(size = 30, weight = FontWeight.Bold, letterSpacing = (-0.025).em),
    headlineMedium = cauriStyle(size = 26, weight = FontWeight.Bold, letterSpacing = (-0.02).em),
    headlineSmall = cauriStyle(size = 24, weight = FontWeight.Bold, letterSpacing = (-0.02).em),
    titleLarge = cauriStyle(size = 20, weight = FontWeight.Bold, letterSpacing = (-0.02).em),
    titleMedium = cauriStyle(size = 17, weight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
    titleSmall = cauriStyle(size = 15, weight = FontWeight.SemiBold),
    bodyLarge = cauriStyle(size = 16, weight = FontWeight.Normal),
    bodyMedium = cauriStyle(size = 15, weight = FontWeight.Normal),
    bodySmall = cauriStyle(size = 13, weight = FontWeight.Normal),
    labelLarge = cauriStyle(size = 14, weight = FontWeight.SemiBold),
    labelMedium = cauriStyle(size = 13, weight = FontWeight.SemiBold),
    labelSmall = cauriStyle(size = 12, weight = FontWeight.SemiBold),
)
