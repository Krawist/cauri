package com.cauri.core.capture.signal

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.RawSignal
import com.cauri.core.parsing.model.ParsedMessage

/** Turns what a template read in a signal into the operation Cauri records. */
interface DetectionService {
    /**
     * The operation [parsed] reports on [account], read in [signal]: a withdrawal or a deposit moves cash, any other
     * debit is an expense and any other credit an income. An operation in another currency than the account's keeps its
     * amount aside, with an amount of 0 and no fee, until the user types what it cost.
     */
    fun detect(signal: RawSignal, parsed: ParsedMessage, account: Account): DetectedOperation
}
