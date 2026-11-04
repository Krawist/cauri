package com.cauri.core.data.service

import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [BalanceService]. */
@Singleton
internal class BalanceServiceImpl @Inject constructor() : BalanceService {
    override fun afterOperation(read: Long, previous: Long?, change: Long): Long =
        if (previous != null && change != 0L && read == previous) previous + change else read
}
