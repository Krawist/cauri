package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AccountDao {
    /** Inserts [account] and returns its generated id. */
    @Insert
    suspend fun insert(account: AccountEntity): Long

    /** Inserts [accounts] in one statement: all or none. */
    @Insert
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    /** Deletes [account]; the database refuses to delete the Cash account. */
    @Delete
    suspend fun delete(account: AccountEntity)

    /** The account of [provider], hidden or not; `null` when the user has none. */
    @Query("SELECT * FROM accounts WHERE provider = :provider ORDER BY id LIMIT 1")
    suspend fun getByProvider(provider: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    /** Every account, hidden or not. */
    @Query("SELECT * FROM accounts ORDER BY id")
    fun observeAll(): Flow<List<AccountEntity>>

    /** The active accounts, Cash first. */
    @Query("SELECT * FROM accounts WHERE status = 'ACTIVE' ORDER BY id")
    fun observeActive(): Flow<List<AccountEntity>>
}
