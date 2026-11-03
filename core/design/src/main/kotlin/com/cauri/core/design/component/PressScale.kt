package com.cauri.core.design.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.cauri.core.design.theme.CauriMotion
import com.cauri.core.design.theme.CauriTheme

// A key or a chip held down shrinks to 96 % of its size.
private const val PRESSED_SCALE = 0.96f

/**
 * A key, a chip or a tile that answers the touch: it sinks a little while held, and comes back when let go. It only
 * watches the finger; the click stays with the element's own `clickable` or `selectable`.
 */
fun Modifier.pressScale(): Modifier = composed {
    val motion = CauriTheme.motion
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) PRESSED_SCALE else 1f,
        animationSpec = motion.tween(CauriMotion.SHORT),
        label = "press scale",
    )
    this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                pressed = false
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}
