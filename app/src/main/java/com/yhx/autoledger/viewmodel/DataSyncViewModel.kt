package com.yhx.autoledger.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.LedgerDao
import com.yhx.autoledger.data.entity.CategoryEntity
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutoLedgerBackup(
    val magicHeader: String = "AUTO_LEDGER_BACKUP_V1",
    val exportTime: Long = System.currentTimeMillis(),
    val ledgers: List<LedgerEntity>,
    val categories: List<CategoryEntity>
)

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val error: String) : SyncState()
}

@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao,
    private val userPrefsRepository: UserPreferencesRepository // ✨ 注入 Preference 获取当前账本
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    fun resetState() {
        _syncState.value = SyncState.Idle
    }

    fun exportData(context: Context, uri: Uri) {
        _syncState.value = SyncState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✨ 导出当前正在查看的账本数据
                val currentBookId = userPrefsRepository.currentBookId.first()
                val ledgers = ledgerDao.getAllLedgersDesc(currentBookId).first()
                val categories = categoryDao.getAllCategories().first()

                val backup = AutoLedgerBackup(ledgers = ledgers, categories = categories)
                val jsonString = Gson().toJson(backup)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                _syncState.value = SyncState.Success("当前账本数据成功导出至本地！")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("导出失败: ${e.localizedMessage}")
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        _syncState.value = SyncState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }

                if (jsonString.isNullOrBlank()) {
                    _syncState.value = SyncState.Error("选中的文件为空！")
                    return@launch
                }

                val backup = try {
                    Gson().fromJson(jsonString, AutoLedgerBackup::class.java)
                } catch (e: Exception) {
                    _syncState.value = SyncState.Error("非法文件：无法识别的格式")
                    return@launch
                }

                if (backup == null || backup.magicHeader != "AUTO_LEDGER_BACKUP_V1") {
                    _syncState.value = SyncState.Error("防伪校验失败！请选择 AutoLedger 专属的 .aldata 文件")
                    return@launch
                }

                if (backup.categories.isNotEmpty()) {
                    categoryDao.insertAll(backup.categories)
                }

                // ✨ 核心兼容代码：旧数据缺少 bookId (Gson 解析为 0L)，自动归为 1L 日常账本
                val compatibleLedgers = backup.ledgers.map { ledger ->
                    if (ledger.bookId == 0L) {
                        ledger.copy(bookId = 1L)
                    } else {
                        ledger
                    }
                }

                if (compatibleLedgers.isNotEmpty()) {
                    compatibleLedgers.forEach { ledgerDao.insertLedger(it) }
                }

                _syncState.value = SyncState.Success("完美恢复！共导入 ${compatibleLedgers.size} 条账单。")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("导入失败，文件可能已损坏。")
            }
        }
    }
}