package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cauri.core.data.model.ReminderAction
import java.time.Instant

/**
 * A decision of the reminder engine, kept to train the bandit. It stores the feature vector as computed at decision
 * time, not references: the context may change afterwards.
 *
 * @property id Generated identifier.
 * @property decidedAt When the decision was taken.
 * @property visitId Visit the decision was about; `null` when it was about a transaction.
 * @property transactionId Detected transaction the decision was about; `null` when it was about a visit.
 * @property features Feature vector at decision time, in the order of [featureVersion].
 * @property featureVersion Version of the feature layout.
 * @property action Action taken.
 * @property probability Probability of [action] under the policy that decided, between 0 and 1.
 * @property modelVersion Version of the rules or bandit model that decided.
 * @property outcome What happened afterwards, the reward reason; `null` while the decision is still open.
 * @property outcomeAt When the outcome was recorded; `null` while the decision is still open.
 */
@Entity(
    tableName = "decision_log",
    foreignKeys = [
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("visitId"), Index("transactionId"), Index("decidedAt")],
)
internal data class DecisionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val decidedAt: Instant,
    val visitId: Long?,
    val transactionId: Long?,
    val features: List<Double>,
    val featureVersion: Int,
    val action: ReminderAction,
    val probability: Double,
    val modelVersion: String,
    val outcome: DecisionOutcome?,
    val outcomeAt: Instant?,
)
