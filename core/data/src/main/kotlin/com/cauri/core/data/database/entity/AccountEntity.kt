package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CASH_PROVIDER

/**
 * An account where the user's money lives. The Cash account (id [CASH_ACCOUNT_ID]) is created with the database and
 * cannot be deleted.
 *
 * @property id Generated identifier.
 * @property name Name shown to the user (« MTN MoMo », « Afriland »).
 * @property kind Cash, mobile money or bank.
 * @property provider Stable code of the operator, bank or card, such as « MTN_MOMO » or « UBA »; [CASH_PROVIDER] for
 *   the Cash account.
 * @property currency ISO 4217 code of the amounts of this account's transactions, such as `XAF`.
 * @property status Active, or hidden by the user (its transactions are kept).
 */
@Entity(tableName = "accounts")
internal data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: AccountKind,
    val provider: String,
    val currency: String,
    val status: AccountStatus,
) {
    companion object {
        const val CASH_ACCOUNT_ID = 1L
    }
}
