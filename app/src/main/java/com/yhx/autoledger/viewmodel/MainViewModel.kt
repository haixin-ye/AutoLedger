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
import kotlinx.coroutines.flow.flow // ✨ 补全了 flow 的导包
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookDao: AccountBookDao // 注入 Dao
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
}