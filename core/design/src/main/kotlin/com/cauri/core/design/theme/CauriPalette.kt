package com.cauri.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Cauri palette for one mode (light or dark). Gold is reserved for cash and savings, coral for spending, green for
 * income, lagoon (accent) for actions and selection. Every `xxxText` colour reaches 4.5:1 on [background].
 *
 * @property background Screen background: pearl in light mode, night in dark mode.
 * @property ink Main text and icons.
 * @property muted Secondary text: captions, hints, inactive tabs.
 * @property glass Translucent fill of glass surfaces (cards, lists, small buttons).
 * @property glassStrong More opaque glass fill, for the floating bottom bar.
 * @property glassBorder Hairline border drawn around every glass surface.
 * @property field Fill of text fields and amount inputs.
 * @property sheet Fill of bottom sheets.
 * @property segment Fill of the selected item of a segmented control.
 * @property chip Fill of unselected chips and small round buttons; also list separators.
 * @property track Background of progress bars.
 * @property trackStrong Background of progress bars drawn on glass.
 * @property handle Drag handle of sheets and dashed borders.
 * @property scrim Dimming layer behind a sheet.
 * @property shadow Colour of the shadows under raised surfaces; opaque, as Android sets the shadow strength itself.
 * @property accent Lagoon: main buttons, selection, the active tab.
 * @property onAccent Text and icons drawn on [accent].
 * @property accentSoft Light lagoon fill of selected chips and badges.
 * @property accentText Lagoon text, links and secondary actions.
 * @property gold Cash and savings: borders and indicators.
 * @property goldSoft Light gold fill of cash elements.
 * @property goldText Gold text: cash labels and counts.
 * @property coral Spending: icons and indicators.
 * @property coralSoft Light coral fill of spending icons.
 * @property coralText Coral text: missing amounts, overruns, destructive actions.
 * @property greenSoft Light green fill of income icons.
 * @property greenText Green text: income amounts.
 * @property blobLagoon Blurred lagoon shape drawn behind the content of a screen.
 * @property blobGold Blurred gold shape drawn behind the content of a screen.
 * @property blobCoral Blurred coral shape drawn behind the content of a screen.
 * @property mtn MTN's yellow, behind the MTN MoMo mark of an account; the same in both themes.
 * @property onMtn Text drawn on [mtn].
 * @property orange Orange's orange, behind the Orange Money mark of an account; the same in both themes.
 * @property onOrange Text drawn on [orange].
 */
@Immutable
data class CauriPalette(
    val background: Color,
    val ink: Color,
    val muted: Color,
    val glass: Color,
    val glassStrong: Color,
    val glassBorder: Color,
    val field: Color,
    val sheet: Color,
    val segment: Color,
    val chip: Color,
    val track: Color,
    val trackStrong: Color,
    val handle: Color,
    val scrim: Color,
    val shadow: Color,
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
    val greenSoft: Color,
    val greenText: Color,
    val blobLagoon: Color,
    val blobGold: Color,
    val blobCoral: Color,
    val mtn: Color,
    val onMtn: Color,
    val orange: Color,
    val onOrange: Color,
)

internal val LightPalette = CauriPalette(
    background = Color(0xFFF6F2EA),
    ink = Color(0xFF0E1A1C),
    muted = Color(0xFF55666A),
    glass = Color(0x85FFFFFF),
    glassStrong = Color(0xA8FFFFFF),
    glassBorder = Color(0xCCFFFFFF),
    field = Color(0xADFFFFFF),
    sheet = Color(0xCCFAF8F3),
    segment = Color(0xFFFFFFFF),
    chip = Color(0x120E1A1C),
    track = Color(0x170E1A1C),
    trackStrong = Color(0x1F0E1A1C),
    handle = Color(0x380E1A1C),
    scrim = Color(0x470E1A1C),
    shadow = Color(0xFF0E1A1C),
    accent = Color(0xFF0F7B76),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0x240F7B76),
    accentText = Color(0xFF0B5E5A),
    gold = Color(0xFFC9963F),
    goldSoft = Color(0x59E8C27A),
    goldText = Color(0xFF7A5719),
    coral = Color(0xFFE2614B),
    coralSoft = Color(0x26E2614B),
    // Darker than the mockups' #C24A35 (4.35:1 on pearl) to reach 4.5:1.
    coralText = Color(0xFFBE4834),
    greenSoft = Color(0x292E9E6A),
    greenText = Color(0xFF23774F),
    blobLagoon = Color(0x802BB3A8),
    blobGold = Color(0x99E8C27A),
    blobCoral = Color(0x6BF0997B),
    mtn = Color(0xFFFFCC00),
    onMtn = Color(0xFF1A1A1A),
    orange = Color(0xFFFF7900),
    onOrange = Color(0xFFFFFFFF),
)

internal val DarkPalette = CauriPalette(
    background = Color(0xFF0B1214),
    ink = Color(0xFFEEF3F2),
    muted = Color(0xFF93A3A5),
    glass = Color(0x7A1E2C2F),
    glassStrong = Color(0x9E1E2C2F),
    glassBorder = Color(0x17FFFFFF),
    field = Color(0xB31E2C2F),
    sheet = Color(0xD1121C1E),
    segment = Color(0x24FFFFFF),
    chip = Color(0x14FFFFFF),
    track = Color(0x1AFFFFFF),
    trackStrong = Color(0x29FFFFFF),
    handle = Color(0x40FFFFFF),
    scrim = Color(0x73000000),
    shadow = Color(0xFF000000),
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
    greenSoft = Color(0x2E4CBE87),
    greenText = Color(0xFF7FD6A8),
    blobLagoon = Color(0x8C0F7B76),
    blobGold = Color(0x738A6420),
    blobCoral = Color(0x668E3B2C),
    mtn = Color(0xFFFFCC00),
    onMtn = Color(0xFF1A1A1A),
    orange = Color(0xFFFF7900),
    onOrange = Color(0xFFFFFFFF),
)

internal val LocalCauriPalette = staticCompositionLocalOf { LightPalette }
