package com.cauri.core.design.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private const val DRAGGED_SCALE = 1.03f

/**
 * [items] one under the other, each [step] tall and [gap] apart, that the finger reorders: picked up as [pickUp] says,
 * the row follows the finger, swaps with a neighbour once past half of it, and [onReorder] gets the new order when the
 * finger lifts. [row] draws an item, told whether it is the one moving, with the modifier that listens to the finger
 * (a handle, or the whole row).
 */
// One parameter per way the lists of the app differ: what picks a row up, its height and the space between rows.
@Suppress("LongParameterList")
@Composable
fun <T> ReorderableColumn(
    items: List<T>,
    key: (T) -> Any,
    step: Dp,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    pickUp: ReorderPickUp = ReorderPickUp.LONG_PRESS,
    gap: Dp = 0.dp,
    row: @Composable (item: T, moving: Boolean, drag: Modifier) -> Unit,
) {
    // One state while the column lives, which the drags read: it follows the items once they are saved.
    var order by remember { mutableStateOf(items) }
    var dragged by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(items) { if (dragged == null) order = items }
    var offset by remember { mutableFloatStateOf(0f) }
    val haptics = rememberCauriHaptics()
    val stepPx = with(LocalDensity.current) { (step + gap).toPx() }
    val drop = {
        dragged = null
        offset = 0f
        onReorder(order)
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
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
                        Modifier.reorderDrag(
                            id = id,
                            pickUp = pickUp,
                            onStart = {
                                haptics.hold()
                                dragged = id
                                offset = 0f
                            },
                            onMove = { amount ->
                                offset += amount
                                val from = order.indexOfFirst { key(it) == id }
                                val to = (from + (offset / stepPx).roundToInt()).coerceIn(0, order.lastIndex)
                                if (to != from) {
                                    // A step felt each time the row passes another.
                                    haptics.step()
                                    order = order.toMutableList().apply { add(to, removeAt(from)) }
                                    offset -= (to - from) * stepPx
                                }
                            },
                            onDrop = drop,
                        ),
                    )
                }
            }
        }
    }
}

// The drag of the row [id], picked up as [pickUp] says: [onMove] gets each vertical move, [onDrop] the end.
private fun Modifier.reorderDrag(
    id: Any,
    pickUp: ReorderPickUp,
    onStart: () -> Unit,
    onMove: (Float) -> Unit,
    onDrop: () -> Unit,
) = pointerInput(id, pickUp) {
    val move = { change: PointerInputChange, amount: Offset ->
        change.consume()
        onMove(amount.y)
    }
    when (pickUp) {
        ReorderPickUp.TOUCH -> detectDragGestures(
            onDragStart = { onStart() },
            onDragEnd = onDrop,
            onDragCancel = onDrop,
            onDrag = move,
        )
        ReorderPickUp.LONG_PRESS -> detectDragGesturesAfterLongPress(
            onDragStart = { onStart() },
            onDragEnd = onDrop,
            onDragCancel = onDrop,
            onDrag = move,
        )
    }
}
