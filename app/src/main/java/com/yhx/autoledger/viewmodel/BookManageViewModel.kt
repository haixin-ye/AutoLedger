package com.yhx.autoledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.dao.AccountBookDao
import com.yhx.autoledger.data.entity.AccountBookEntity
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookManageViewModel @Inject constructor(
    private val bookDao: AccountBookDao,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    // 获取数据库中所有的账本
    val allBooks: StateFlow<List<AccountBookEntity>> = bookDao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 获取当前正在使用的账本 ID
    val currentBookId: StateFlow<Long> = userPrefs.currentBookId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    // 切换账本（更新 DataStore）
    fun switchBook(bookId: Long) {
        viewModelScope.launch {
            userPrefs.updateCurrentBookId(bookId)
        }
    }

    // 新增账本
    fun addBook(name: String, colorInt: Int) {
        viewModelScope.launch {
            val newBook = AccountBookEntity(
                name = name,
                coverColor = colorInt,
                isSystemDefault = false
            )
            bookDao.insertBook(newBook)
        }
    }

    // 删除账本
    fun deleteBook(book: AccountBookEntity) {
        if (book.isSystemDefault) return // 预设账本不可删除
        viewModelScope.launch {
            // 如果删除的刚好是当前正在用的账本，自动切回 1号日常账本
            if (currentBookId.value == book.id) {
                switchBook(1L)
            }
            bookDao.deleteBook(book)
        }
    }
}