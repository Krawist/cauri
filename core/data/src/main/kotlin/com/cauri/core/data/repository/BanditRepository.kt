package com.cauri.core.data.repository

import com.cauri.core.data.model.BanditSnapshot
import com.cauri.core.data.model.NewBanditSnapshot

/** The models of the reminders bandit: the latest decides once published, the rules before. */
interface BanditRepository {
    /** The latest model; `null` while the rules decide. */
    suspend fun latest(): BanditSnapshot?

    /** Publishes [model] in one write, as the next version, and returns it. */
    suspend fun publish(model: NewBanditSnapshot): Long

    /** Forgets every model: the rules decide again until the bandit learns enough. */
    suspend fun deleteAll()
}
