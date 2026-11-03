package com.cauri.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.dao.DecisionLogDao
import com.cauri.core.data.database.dao.HabitDao
import com.cauri.core.data.database.dao.PlaceDao
import com.cauri.core.data.database.dao.RawSignalDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.dao.VisitDao
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.HabitEntity
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.VisitEntity

/** Local database of Cauri. Build it with [buildCauriDatabase], which creates the Cash account. */
@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        RawSignalEntity::class,
        PlaceEntity::class,
        VisitEntity::class,
        TransactionEntity::class,
        CashCountEntity::class,
        DecisionLogEntity::class,
        HabitEntity::class,
    ],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(DatabaseConverters::class)
internal abstract class CauriDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun categoryDao(): CategoryDao

    abstract fun rawSignalDao(): RawSignalDao

    abstract fun placeDao(): PlaceDao

    abstract fun visitDao(): VisitDao

    abstract fun transactionDao(): TransactionDao

    abstract fun cashCountDao(): CashCountDao

    abstract fun decisionLogDao(): DecisionLogDao

    abstract fun habitDao(): HabitDao
}
