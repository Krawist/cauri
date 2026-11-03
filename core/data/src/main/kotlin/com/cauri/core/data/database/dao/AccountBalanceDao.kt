package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cauri.core.data.database.entity.AccountBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AccountBalanceDao {
    /** The latest balance of each account that has one; of two balances at the same time, the last recorded. */
    @Query(
        "SELECT * FROM account_balances AS b WHERE NOT EXISTS (SELECT 1 FROM account_balances AS o " +
            "WHERE o.accountId = b.accountId AND (o.at > b.at OR (o.at = b.at AND o.id > b.id)))",
    )
    fun observeLatest(): Flow<List<AccountBalanceEntity>>
}
