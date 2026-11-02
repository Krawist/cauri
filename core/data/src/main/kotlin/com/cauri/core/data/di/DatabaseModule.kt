package com.cauri.core.data.di

import android.content.Context
import androidx.room.Room
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.buildCauriDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CauriDatabase =
        Room.databaseBuilder(context, CauriDatabase::class.java, "cauri.db").buildCauriDatabase()

    @Provides
    fun provideAccountDao(database: CauriDatabase) = database.accountDao()

    @Provides
    fun provideCategoryDao(database: CauriDatabase) = database.categoryDao()

    @Provides
    fun provideRawSignalDao(database: CauriDatabase) = database.rawSignalDao()

    @Provides
    fun providePlaceDao(database: CauriDatabase) = database.placeDao()

    @Provides
    fun provideVisitDao(database: CauriDatabase) = database.visitDao()

    @Provides
    fun provideTransactionDao(database: CauriDatabase) = database.transactionDao()

    @Provides
    fun provideCashCountDao(database: CauriDatabase) = database.cashCountDao()

    @Provides
    fun provideDecisionLogDao(database: CauriDatabase) = database.decisionLogDao()
}
