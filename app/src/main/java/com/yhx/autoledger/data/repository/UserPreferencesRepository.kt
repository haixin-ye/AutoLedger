package com.yhx.autoledger.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey // 导入 long 类型 key
import androidx.datastore.preferences.core.stringPreferencesKey
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
    // ================== 定义所有的 Keys ==================
    // 主题的 Key
    private val THEME_KEY = intPreferencesKey("theme_preference")

    // 账本的 Key
    private val CURRENT_BOOK_ID_KEY = longPreferencesKey("current_book_id")

    // AI 自定义记忆指令的 Key
    private val AI_CUSTOM_INSTRUCTIONS_KEY = stringPreferencesKey("ai_custom_instructions")

    // ✨ 新增 Keys
    private val PRIVACY_LOCK_KEY = booleanPreferencesKey("privacy_lock_enabled")
    private val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")

    private val PRIVACY_LOCK_PATTERN_KEY = stringPreferencesKey("privacy_lock_pattern")

    // ================== 账本相关 ==================
    // 获取当前账本 ID
    val currentBookId: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_BOOK_ID_KEY] ?: 1L
    }

    // 保存切换的账本 ID
    suspend fun updateCurrentBookId(bookId: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_BOOK_ID_KEY] = bookId
        }
    }


    // ================== 主题相关 ==================
    // 获取当前主题设置
    val themePreference: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: 0 // 默认 0：跟随系统
    }

    // 更新主题设置
    suspend fun updateThemePreference(themeValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeValue
        }
    }


    // ================== 预算相关 ==================
    // 接收动态的年月 Key，返回对应的预算
    fun getMonthlyBudget(yearMonthKey: String): Flow<Double> {
        val key = doublePreferencesKey("budget_$yearMonthKey")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 5000.0 // 默认预算
        }
    }

    // 保存特定月份的预算
    suspend fun updateMonthlyBudget(yearMonthKey: String, newBudget: Double) {
        val key = doublePreferencesKey("budget_$yearMonthKey")
        context.dataStore.edit { preferences ->
            preferences[key] = newBudget
        }
    }

    // ================== AI 记忆相关 ==================
    val aiCustomInstructions: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AI_CUSTOM_INSTRUCTIONS_KEY] ?: ""
    }

    suspend fun updateAiCustomInstructions(instructions: String) {
        context.dataStore.edit { preferences ->
            preferences[AI_CUSTOM_INSTRUCTIONS_KEY] = instructions
        }
    }

    // ================== 隐私锁与提醒 ==================
    val privacyLockEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[PRIVACY_LOCK_KEY] ?: false }

    val reminderTime: Flow<String> = context.dataStore.data.map { it[REMINDER_TIME_KEY] ?: "" }

    suspend fun updatePrivacyLock(enabled: Boolean) {
        context.dataStore.edit { it[PRIVACY_LOCK_KEY] = enabled }
    }

    suspend fun updateReminderTime(time: String) {
        context.dataStore.edit { it[REMINDER_TIME_KEY] = time }
    }

    val privacyLockPattern: Flow<String> =
        context.dataStore.data.map { it[PRIVACY_LOCK_PATTERN_KEY] ?: "" }

    suspend fun updatePrivacyPattern(pattern: String) {
        context.dataStore.edit { it[PRIVACY_LOCK_PATTERN_KEY] = pattern }

    }

    private val AI_PERSONA_ID_KEY = stringPreferencesKey("ai_persona_id")

    // 2. 在类中补充读写方法：
    // 默认返回 "professional_butler" (专业管家)
    val aiPersonaId: Flow<String> = context.dataStore.data.map {
        it[AI_PERSONA_ID_KEY] ?: "professional_butler"
    }

    suspend fun updateAiPersonaId(id: String) {
        context.dataStore.edit { it[AI_PERSONA_ID_KEY] = id }
    }
}