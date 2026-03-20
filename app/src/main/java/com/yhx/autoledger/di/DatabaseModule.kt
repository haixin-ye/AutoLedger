package com.yhx.autoledger.di

import android.content.Context
import com.yhx.autoledger.data.AppDatabase
import com.yhx.autoledger.data.dao.AccountBookDao
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.LedgerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideLedgerDao(database: AppDatabase): LedgerDao {
        return database.ledgerDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    // ✨ 新增：告诉 Hilt 如何提供 AccountBookDao
    @Provides
    fun provideAccountBookDao(database: AppDatabase): AccountBookDao {
        return database.accountBookDao()
    }
}