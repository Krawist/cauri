package com.cauri.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cauri.core.data.database.dao.AccountBalanceDao
import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.dao.AvailableDao
import com.cauri.core.data.database.dao.BanditModelDao
import com.cauri.core.data.database.dao.BudgetDao
import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.dao.ConfirmationDao
import com.cauri.core.data.database.dao.DecisionLogDao
import com.cauri.core.data.database.dao.DiscoveryDao
import com.cauri.core.data.database.dao.FixedCostDao
import com.cauri.core.data.database.dao.GoalDao
import com.cauri.core.data.database.dao.HabitDao
import com.cauri.core.data.database.dao.IncomeSplitDao
import com.cauri.core.data.database.dao.InsightsDao
import com.cauri.core.data.database.dao.PlaceDao
import com.cauri.core.data.database.dao.RawSignalDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.dao.TransactionMergeDao
import com.cauri.core.data.database.dao.TransferLinkDao
import com.cauri.core.data.database.dao.VisitDao
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.BanditModelEntity
import com.cauri.core.data.database.entity.BudgetEntity
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.FixedCostEntity
import com.cauri.core.data.database.entity.GoalContributionEntity
import com.cauri.core.data.database.entity.GoalEntity
import com.cauri.core.data.database.entity.HabitEntity
import com.cauri.core.data.database.entity.IncomeSplitEntity
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.StopEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionMergeEntity
import com.cauri.core.data.database.entity.TransferLinkEntity
import com.cauri.core.data.database.entity.VisitEntity

/** Local database of Cauri. Build it with [buildCauriDatabase], which creates the Cash account. */
// One accessor per DAO, as Room needs: the count grows with the tables.
@Suppress("TooManyFunctions")
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
        TransactionMergeEntity::class,
        TransferLinkEntity::class,
        AccountBalanceEntity::class,
        StopEntity::class,
        PlaceSuggestionEntity::class,
        BanditModelEntity::class,
        BudgetEntity::class,
        FixedCostEntity::class,
        IncomeSplitEntity::class,
        GoalEntity::class,
        GoalContributionEntity::class,
    ],
    version = 23,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17, spec = RemoveVisitOutcome::class),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
    ],
)
@TypeConverters(DatabaseConverters::class)
internal abstract class CauriDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun accountBalanceDao(): AccountBalanceDao

    abstract fun categoryDao(): CategoryDao

    abstract fun confirmationDao(): ConfirmationDao

    abstract fun rawSignalDao(): RawSignalDao

    abstract fun placeDao(): PlaceDao

    abstract fun visitDao(): VisitDao

    abstract fun transactionDao(): TransactionDao

    abstract fun transactionMergeDao(): TransactionMergeDao

    abstract fun transferLinkDao(): TransferLinkDao

    abstract fun cashCountDao(): CashCountDao

    abstract fun decisionLogDao(): DecisionLogDao

    abstract fun banditModelDao(): BanditModelDao

    abstract fun budgetDao(): BudgetDao

    abstract fun availableDao(): AvailableDao

    abstract fun fixedCostDao(): FixedCostDao

    abstract fun incomeSplitDao(): IncomeSplitDao

    abstract fun goalDao(): GoalDao

    abstract fun insightsDao(): InsightsDao

    abstract fun habitDao(): HabitDao

    abstract fun discoveryDao(): DiscoveryDao
}
