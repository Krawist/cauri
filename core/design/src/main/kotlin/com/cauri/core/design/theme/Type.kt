package com.cauri.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// TODO(CAU-02): embed Inter (OFL) in res/font and use it here.
private val CauriFontFamily = FontFamily.Default

val CauriTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CauriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        letterSpacing = (-0.035).em,
        fontFeatureSettings = "tnum",
    ),
    headlineMedium = TextStyle(
        fontFamily = CauriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = (-0.02).em,
    ),
    titleMedium = TextStyle(fontFamily = CauriFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = CauriFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = CauriFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)
