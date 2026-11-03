package com.cauri.core.capture.health

/** Shows the warning that Cauri receives no more messages. */
interface CaptureHealthNotifier {
    /** Shows the warning, which opens Réglages; `false` when the user does not allow Cauri's notifications. */
    fun show(): Boolean
}
