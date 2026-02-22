package com.yhx.autoledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import com.yhx.autoledger.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // 告诉 Hilt 这是一个 ViewModel，需要它来负责注入
class HomeViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val userPrefsRepository: UserPreferencesRepository
) : ViewModel() {


    // 记录当前选中的账单 ID 集合
    private val _selectedLedgerIds: MutableStateFlow<Set<Long>> = MutableStateFlow<Set<Long>>(
        emptySet()
    )

    // ✨ 核心状态：月份偏移量（0是本月，-1是上月...）
    val monthOffset = MutableStateFlow(0)
    val selectedLedgerIds: StateFlow<Set<Long>> = _selectedLedgerIds.asStateFlow()

    // 💡 只要 monthOffset 发生改变，下面所有的流都会自动重新去数据库查询对应月份的数据！
    val recentLedgers: StateFlow<List<LedgerEntity>> = monthOffset.flatMapLatest { offset ->
        repository.getLedgersBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 【2】获取本月总支出 (type = 0)
    val currentMonthExpense: StateFlow<Double> = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(
            DateUtils.getMonthStart(offset),
            DateUtils.getMonthEnd(offset),
            0
        )
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val currentMonthIncome: StateFlow<Double> = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(
            DateUtils.getMonthStart(offset),
            DateUtils.getMonthEnd(offset),
            1
        )
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 用户本月设置的预算
    val monthlyBudget: StateFlow<Double> = monthOffset.flatMapLatest { offset ->
        val key = DateUtils.getYearMonthKey(offset)
        userPrefsRepository.getMonthlyBudget(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    // ================== 用户操作响应 ==================
    // 提供给 UI 切换月份的方法
    fun changeMonth(delta: Int) {
        // 限制不能查看未来月份（最高到 0 本月）
        if (monthOffset.value + delta <= 0) {
            monthOffset.value += delta
        }
    }

    // 提供一个方法，供设置页面调用来修改预算
    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            // ✨ 修改：只更新当前选中的那个月份的预算
            val key = DateUtils.getYearMonthKey(monthOffset.value)
            userPrefsRepository.updateMonthlyBudget(key, newBudget)
        }
    }

    /**
     * 当用户在 UI 界面点击“保存”记账时调用此方法
     */
    fun addLedger(
        amount: Double,
        type: Int,
        categoryName: String,
        categoryIcon: String,
        timestamp: Long, // 接收 UI 传来的时间戳
        note: String
    ) {
        viewModelScope.launch {
            val newLedger = LedgerEntity(
                amount = amount,
                type = type,
                categoryName = categoryName,
                categoryIcon = categoryIcon,
                timestamp = timestamp, // ✅ 正确：使用 UI 组件（ManualAddSheet）传过来的时间
                note = note,
                source = "MANUAL" // 手动记账
            )
            repository.insertLedger(newLedger) // 存入数据库
        }
    }

    // 更新账单
    fun updateLedger(ledger: LedgerEntity) {
        viewModelScope.launch { repository.insertLedger(ledger) }
    }

    // 删除账单
    fun deleteLedger(ledger: LedgerEntity) {
        viewModelScope.launch { repository.deleteLedger(ledger) }
    }

    // 长按或点击时切换选中状态
    fun toggleSelection(ledgerId: Long) {
        val current = _selectedLedgerIds.value.toMutableSet()
        if (current.contains(ledgerId)) {
            current.remove(ledgerId)
        } else {
            current.add(ledgerId)
        }
        _selectedLedgerIds.value = current
    }

    // 全选/取消全选
    fun selectAll(ledgerIds: List<Long>) {
        if (_selectedLedgerIds.value.size == ledgerIds.size) {
            _selectedLedgerIds.value = emptySet() // 如果已经全选，则清空
        } else {
            _selectedLedgerIds.value = ledgerIds.toSet() // 否则全选
        }
    }

    // 清空选择（退出多选模式）
    fun clearSelection() {
        _selectedLedgerIds.value = emptySet()
    }

    // 执行批量删除
    fun deleteSelectedLedgers() {
        val idsToDelete = _selectedLedgerIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            repository.deleteLedgersByIds(idsToDelete)
            clearSelection() // 删除完成后自动清空集合，退出多选模式
        }
    }
}