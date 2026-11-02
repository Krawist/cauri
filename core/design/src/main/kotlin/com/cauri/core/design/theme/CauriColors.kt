package com.cauri.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Cauri palette for one mode (light or dark). Gold is reserved for cash and savings, coral for spending, green for
 * income, lagoon for actions.
 */
@Immutable
data class CauriPalette(
    val background: Color,
    val ink: Color,
    val muted: Color,
    val glass: Color,
    val glassBorder: Color,
    val chip: Color,
    val track: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val gold: Color,
    val goldSoft: Color,
    val goldText: Color,
    val coral: Color,
    val coralSoft: Color,
    val coralText: Color,
    val greenText: Color,
)

val LightPalette = CauriPalette(
    background = Color(0xFFF6F2EA),
    ink = Color(0xFF0E1A1C),
    muted = Color(0xFF55666A),
    glass = Color(0x85FFFFFF),
    glassBorder = Color(0xCCFFFFFF),
    chip = Color(0x120E1A1C),
    track = Color(0x170E1A1C),
    accent = Color(0xFF0F7B76),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0x240F7B76),
    accentText = Color(0xFF0B5E5A),
    gold = Color(0xFFC9963F),
    goldSoft = Color(0x59E8C27A),
    goldText = Color(0xFF7A5719),
    coral = Color(0xFFE2614B),
    coralSoft = Color(0x26E2614B),
    coralText = Color(0xFFC24A35),
    greenText = Color(0xFF23774F),
)

val DarkPalette = CauriPalette(
    background = Color(0xFF0B1214),
    ink = Color(0xFFEEF3F2),
    muted = Color(0xFF93A3A5),
    glass = Color(0x7A1E2C2F),
    glassBorder = Color(0x17FFFFFF),
    chip = Color(0x14FFFFFF),
    track = Color(0x1AFFFFFF),
    accent = Color(0xFF2BB3A8),
    onAccent = Color(0xFF06201E),
    accentSoft = Color(0x2E2BB3A8),
    accentText = Color(0xFF6FD6CC),
    gold = Color(0xFFD9AE5F),
    goldSoft = Color(0x2ED9AE5F),
    goldText = Color(0xFFE8C98E),
    coral = Color(0xFFEE7A65),
    coralSoft = Color(0x2EEE7A65),
    coralText = Color(0xFFF29A88),
    greenText = Color(0xFF7FD6A8),
)

val LocalCauriPalette = staticCompositionLocalOf { LightPalette }
