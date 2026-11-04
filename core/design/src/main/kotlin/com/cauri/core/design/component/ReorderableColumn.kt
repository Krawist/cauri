package com.cauri.core.design.component

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private const val DRAGGED_SCALE = 1.02f

/**
 * [items] one under the other, each [step] tall, that a long press picks up: the row follows the finger, swaps with a
 * neighbour once past half of it, and [onReorder] gets the new order when the finger lifts. [row] draws an item, told
 * whether it is the one moving, with the modifier that listens to the long press (the whole row, or its handle).
 */
@Composable
fun <T> ReorderableColumn(
    items: List<T>,
    key: (T) -> Any,
    step: Dp,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    row: @Composable (item: T, moving: Boolean, drag: Modifier) -> Unit,
) {
    // One state while the column lives, which the drags read: it follows the items once they are saved.
    var order by remember { mutableStateOf(items) }
    var dragged by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(items) { if (dragged == null) order = items }
    var offset by remember { mutableFloatStateOf(0f) }
    val haptics = rememberCauriHaptics()
    val stepPx = with(LocalDensity.current) { step.toPx() }
    val drop = {
        dragged = null
        offset = 0f
        onReorder(order)
    }
    Column(modifier = modifier) {
        order.forEach { item ->
            val id = key(item)
            // Keyed by item: once the order changes, each row keeps its own drag and offset.
            key(id) {
                val moving = id == dragged
                Box(
                    Modifier
                        .zIndex(if (moving) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (moving) offset else 0f
                            scaleX = if (moving) DRAGGED_SCALE else 1f
                            scaleY = if (moving) DRAGGED_SCALE else 1f
                        },
                ) {
                    row(
                        item,
                        moving,
                        Modifier.pointerInput(id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    haptics.hold()
                                    dragged = id
                                    offset = 0f
                                },
                                onDragEnd = drop,
                                onDragCancel = drop,
                                onDrag = { change, amount ->
                                    change.consume()
                                    offset += amount.y
                                    val from = order.indexOfFirst { key(it) == id }
                                    val to = (from + (offset / stepPx).roundToInt()).coerceIn(0, order.lastIndex)
                                    if (to != from) {
                                        // A step felt each time the row passes another.
                                        haptics.step()
                                        order = order.toMutableList().apply { add(to, removeAt(from)) }
                                        offset -= (to - from) * stepPx
                                    }
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}
