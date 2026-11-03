package com.cauri.core.design.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.cauri.core.design.theme.CauriTheme

/**
 * The [items] of a column, each keyed by [key]: one added grows in, one removed fades and closes its place instead of
 * vanishing, as a card answered. The items shown first come without motion.
 */
@Composable
fun <T> AnimatedItems(items: List<T>, key: (T) -> Any, content: @Composable (T) -> Unit) {
    val motion = CauriTheme.motion
    // What is shown: the items, and those leaving at the place they had. Not states: the items drive the composition.
    val shown = remember { mutableListOf<T>() }
    val visibility = remember { mutableMapOf<Any, MutableTransitionState<Boolean>>() }
    val first = visibility.isEmpty()
    val current = items.map(key).toSet()
    val leaving = shown.withIndex().filter { key(it.value) !in current && visibility[key(it.value)]?.isLeft() != true }
    val merged = items.toMutableList()
    leaving.forEach { (index, item) -> merged.add(index.coerceAtMost(merged.size), item) }
    shown.clear()
    shown.addAll(merged)
    merged.forEach { item ->
        val id = key(item)
        val state = visibility.getOrPut(id) { MutableTransitionState(first) }
        state.targetState = id in current
    }
    merged.forEach { item ->
        val id = key(item)
        key(id) {
            val state = visibility.getValue(id)
            AnimatedVisibility(visibleState = state, enter = motion.itemEnter(), exit = motion.itemExit()) {
                content(item)
            }
            // Gone for good once its exit is over: it is dropped at the next composition.
            if (state.isLeft()) {
                shown.remove(item)
                visibility.remove(id)
            }
        }
    }
}

private fun MutableTransitionState<Boolean>.isLeft() = isIdle && !currentState && !targetState
