package com.yhx.autoledger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiMemoryViewModel @Inject constructor(
    private val userPrefs: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 观察自定义指令流
    val customInstructions: StateFlow<String> = userPrefs.aiCustomInstructions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveInstructions(text: String) {
        viewModelScope.launch {
            userPrefs.updateAiCustomInstructions(text)
        }
    }

//    // 清空 AI 的短期对话记忆
//    fun clearChatHistory() {
//        val prefs = context.getSharedPreferences("ai_chat_history", Context.MODE_PRIVATE)
//        prefs.edit().clear().apply()
//    }
}