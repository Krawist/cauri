package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A notification received from a watched sender, kept as is: the source of truth of the capture. Parsed transactions
 * are projections of it and can be rebuilt.
 *
 * @property id Generated identifier.
 * @property sender Package name of the app that posted the notification.
 * @property text Text of the notification.
 * @property receivedAt When the notification was posted.
 * @property templateVersion Version of the template that parsed it; `null` while no template recognises it.
 */
@Entity(tableName = "raw_signals")
internal data class RawSignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val text: String,
    val receivedAt: Instant,
    val templateVersion: String?,
)
