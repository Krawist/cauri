package com.cauri.core.data.repository

import kotlinx.coroutines.flow.Flow

/** What the user can spend today, from the money received this month. */
interface AvailableRepository {
    /**
     * What can be spent each day until the end of the month, in minor units of XAF, again after each change; `null`
     * before the first income of the month.
     */
    val today: Flow<Long?>
}
