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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. 定义我们的专属备份数据结构
data class AutoLedgerBackup(
    val magicHeader: String = "AUTO_LEDGER_BACKUP_V1", // 核心：魔法签名，防伪校验
    val exportTime: Long = System.currentTimeMillis(),
    val ledgers: List<LedgerEntity>,
    val categories: List<CategoryEntity>
)

// 2. UI 状态密封类
sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val error: String) : SyncState()
}

@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    fun resetState() {
        _syncState.value = SyncState.Idle
    }

    // ==========================================
    // 🚀 导出逻辑：将数据库打包为 .aldata 专属文件
    // ==========================================
    fun exportData(context: Context, uri: Uri) {
        _syncState.value = SyncState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✨ 精准对接您的 DAO 方法：getAllLedgersDesc() 和 getAllCategories()
                val ledgers = ledgerDao.getAllLedgersDesc().first()
                val categories = categoryDao.getAllCategories().first()

                // 组装专属数据包
                val backup = AutoLedgerBackup(
                    ledgers = ledgers,
                    categories = categories
                )

                // 转化为 JSON 字符串
                val jsonString = Gson().toJson(backup)

                // 写入到系统指定的 Uri 文件中
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }

                _syncState.value = SyncState.Success("数据成功导出至本地！")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("导出失败: ${e.localizedMessage}")
            }
        }
    }

    // ==========================================
    // 🚀 导入逻辑：双重防伪校验，只认自己的文件！
    // ==========================================
    fun importData(context: Context, uri: Uri) {
        _syncState.value = SyncState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. 读取文件内容
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }

                if (jsonString.isNullOrBlank()) {
                    _syncState.value = SyncState.Error("选中的文件为空！")
                    return@launch
                }

                // 2. 解析 JSON 并进行魔法签名严格校验！
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

                // 3. 校验通过，开始恢复数据
                // ✨ 精准对接：使用您的批量插入 insertAll 提高性能
                if (backup.categories.isNotEmpty()) {
                    categoryDao.insertAll(backup.categories)
                }

                // ✨ 精准对接：使用 insertLedger 循环插入账单
                if (backup.ledgers.isNotEmpty()) {
                    backup.ledgers.forEach { ledgerDao.insertLedger(it) }
                }

                _syncState.value = SyncState.Success("完美恢复！共导入 ${backup.ledgers.size} 条账单。")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("导入失败，文件可能已损坏。")
            }
        }
    }
}