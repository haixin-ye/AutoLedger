package com.yhx.autoledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 从 DataStore 读取主题配置，转换为 StateFlow 供 UI 观察
    // 默认值为 0 (跟随系统)
    val themePreference = userPreferencesRepository.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // 更新主题配置并保存到 DataStore
    fun updateTheme(themeValue: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(themeValue)
        }
    }
}