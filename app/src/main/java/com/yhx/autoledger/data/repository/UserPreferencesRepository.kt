package com.yhx.autoledger.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 创建一个全局的 DataStore 实例，文件名为 "settings.preferences_pb"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ✨ 修改点 1：接收动态的年月 Key，返回对应的预算
    fun getMonthlyBudget(yearMonthKey: String): Flow<Double> {
        val key = doublePreferencesKey("budget_$yearMonthKey")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 5000.0 // 默认预算
        }
    }

    // ✨ 修改点 2：保存特定月份的预算
    suspend fun updateMonthlyBudget(yearMonthKey: String, newBudget: Double) {
        val key = doublePreferencesKey("budget_$yearMonthKey")
        context.dataStore.edit { preferences ->
            preferences[key] = newBudget
        }
    }
}