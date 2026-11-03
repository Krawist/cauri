package com.cauri.core.capture.reminder

import com.cauri.core.engine.BanditModel

/**
 * The model of the bandit that decides.
 *
 * @property version Version it was published as, logged with its decisions (« bandit-3 »).
 * @property model What it learnt.
 */
data class PublishedModel(val version: Long, val model: BanditModel)
