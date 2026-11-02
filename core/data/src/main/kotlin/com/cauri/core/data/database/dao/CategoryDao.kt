package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CategoryDao {
    /** Inserts [category] and returns its generated id. */
    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    /** The active categories, by name. */
    @Query("SELECT * FROM categories WHERE status = 'ACTIVE' ORDER BY name")
    fun observeActive(): Flow<List<CategoryEntity>>
}
