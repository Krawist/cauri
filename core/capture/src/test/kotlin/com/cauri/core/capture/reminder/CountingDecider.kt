package com.cauri.core.capture.reminder

/** [ReminderDecider] for tests: it counts the calls. */
internal class CountingDecider : ReminderDecider {
    var runs = 0

    override suspend fun decidePending() {
        runs++
    }
}
