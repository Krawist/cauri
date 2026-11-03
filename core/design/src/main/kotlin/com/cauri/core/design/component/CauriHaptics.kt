package com.cauri.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The touch language of Cauri: one feeling per kind of gesture, the same everywhere. Android plays them only when the
 * phone's touch vibrations are on.
 */
@Stable
class CauriHaptics(private val feedback: HapticFeedback) {
    /** A key of the keypad, or a « − » and « + » of the count. */
    fun key() = feedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)

    /** A choice made: a chip, a filter, a tab, a row ticked. */
    fun select() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    /** Something saved or confirmed: an expense, a card, a count, a balance. */
    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    /** Something set aside or refused: « Rien dépensé », an amount over the limit. */
    fun reject() = feedback.performHapticFeedback(HapticFeedbackType.Reject)

    /** A long press that opens more: a habit's sheet, a line picked up to be dragged. */
    fun hold() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)

    /** A step of a drag: a line passing another. */
    fun step() = feedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)

    /** The point where letting go of a swipe answers. */
    fun threshold() = feedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
}

/** The haptic gestures of Cauri, for the screen being composed. */
@Composable
fun rememberCauriHaptics(): CauriHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { CauriHaptics(feedback) }
}
