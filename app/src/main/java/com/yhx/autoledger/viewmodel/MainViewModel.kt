package com.yhx.autoledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.dao.AccountBookDao
import com.yhx.autoledger.data.entity.AccountBookEntity
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.yhx.autoledger.utils.ReminderHelper
import com.yhx.autoledger.data.repository.AIPersonaRepository
import com.yhx.autoledger.model.AIPersona

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookDao: AccountBookDao, // 注入 Dao
    private val aiPersonaRepository: AIPersonaRepository,
) : ViewModel() {

    val themePreference = userPreferencesRepository.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val currentBookId = userPreferencesRepository.currentBookId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    // ✨ 核心修复：强制声明类型为 StateFlow<AccountBookEntity?>，这样外部调用时就能认出 name 属性了！
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentBook: StateFlow<AccountBookEntity?> = currentBookId.flatMapLatest { id ->
        flow {
            emit(bookDao.getBookById(id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateTheme(themeValue: Int) {
        viewModelScope.launch { userPreferencesRepository.updateThemePreference(themeValue) }
    }

    fun switchBook(bookId: Long) {
        viewModelScope.launch { userPreferencesRepository.updateCurrentBookId(bookId) }
    }

    // ✨ 暴露给 UI 的流
    val privacyLockEnabled = userPreferencesRepository.privacyLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderTime = userPreferencesRepository.reminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // ✨ UI 更新操作
    fun setPrivacyLock(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.updatePrivacyLock(enabled) }
    }

    fun setReminderTime(context: Context, time: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateReminderTime(time)
            ReminderHelper.scheduleReminder(context, time) // 更新系统闹钟
        }
    }

    val privacyLockPattern = userPreferencesRepository.privacyLockPattern
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setPrivacyPattern(pattern: String) {
        viewModelScope.launch { userPreferencesRepository.updatePrivacyPattern(pattern) }
    }

    // 3. 在类中追加状态流和更新方法：
    // ✨ 获取所有可用的人设列表
    val allPersonas: List<AIPersona> = aiPersonaRepository.getAllPersonas()

    // ✨ 获取当前选中的人设 ID
    val aiPersonaId = userPreferencesRepository.aiPersonaId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "professional_butler")

    fun setAiPersonaId(id: String) {
        viewModelScope.launch { userPreferencesRepository.updateAiPersonaId(id) }
    }
}