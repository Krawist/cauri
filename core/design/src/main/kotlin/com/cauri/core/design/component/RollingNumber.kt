package com.cauri.core.design.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.cauri.core.design.theme.CauriMotion
import com.cauri.core.design.theme.CauriTheme

/**
 * An amount that rolls to its new [value]: the new one comes up from below when it grew, down from above when it
 * shrank, as a counter. [content] draws the amount; it is drawn as is when motion is reduced.
 */
@Composable
fun RollingNumber(
    value: Long,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (Long) -> Unit,
) {
    val motion = CauriTheme.motion
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        contentAlignment = contentAlignment,
        transitionSpec = {
            if (motion.reduced) {
                ContentTransform(EnterTransition.None, ExitTransition.None)
            } else {
                val up = targetState > initialState
                (
                    slideInVertically(motion.tween<IntOffset>(CauriMotion.MEDIUM, CauriMotion.Emphasized)) {
                        if (up) it else -it
                    } + fadeIn(motion.tween(CauriMotion.SHORT))
                    ).togetherWith(
                    slideOutVertically(motion.tween<IntOffset>(CauriMotion.SHORT, CauriMotion.Exit)) {
                        if (up) -it else it
                    } + fadeOut(motion.tween(CauriMotion.SHORT)),
                ).using(SizeTransform(clip = false))
            }
        },
        label = "rolling number",
    ) { content(it) }
}
