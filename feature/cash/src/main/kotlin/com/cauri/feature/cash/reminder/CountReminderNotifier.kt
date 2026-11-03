package com.cauri.feature.cash.reminder

import com.cauri.core.data.model.CountReminderReason

/** Shows the cash count reminder. */
internal interface CountReminderNotifier {
    /** Shows the reminder for [reason], opening the cash count; `false` when Cauri may not show notifications. */
    fun show(reason: CountReminderReason): Boolean
}
