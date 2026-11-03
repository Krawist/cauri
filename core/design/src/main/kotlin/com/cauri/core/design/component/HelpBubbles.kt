package com.cauri.core.design.component

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Which help bubbles show: one at a time, the first asked for that the user never closed, each until it is closed and
 * never again. The app provides it; screens only place their bubbles ([HelpBubble]).
 */
interface HelpBubbles {
    /** Whether the bubble [id] is the one to show now. */
    fun isShown(id: String): Boolean

    /** The bubble [id] is on screen and asks to be shown, after those that asked before. */
    fun request(id: String)

    /** The bubble [id] left the screen: the next one may show. */
    fun release(id: String)

    /** The user closed the bubble [id]: it never shows again. */
    fun close(id: String)
}

/** The help bubbles of the app; none show without a provider, as in previews. */
val LocalHelpBubbles = staticCompositionLocalOf<HelpBubbles> { NoHelpBubbles }

private object NoHelpBubbles : HelpBubbles {
    override fun isShown(id: String) = false

    override fun request(id: String) = Unit

    override fun release(id: String) = Unit

    override fun close(id: String) = Unit
}
