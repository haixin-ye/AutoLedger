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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val userPrefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedLedgerIds: MutableStateFlow<Set<Long>> = MutableStateFlow(emptySet())
    val selectedLedgerIds: StateFlow<Set<Long>> = _selectedLedgerIds.asStateFlow()

    val monthOffset = MutableStateFlow(0)

    // ✨ 获取全局的账本 ID 流
    val currentBookId: StateFlow<Long> = userPrefsRepository.currentBookId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    // ✨ 核心魔法：联合监听 (月份 + 账本ID)
    val recentLedgers: StateFlow<List<LedgerEntity>> =
        combine(monthOffset, currentBookId) { offset, bookId ->
            Pair(offset, bookId)
        }.flatMapLatest { (offset, bookId) ->
            repository.getLedgersBetween(
                bookId,
                DateUtils.getMonthStart(offset),
                DateUtils.getMonthEnd(offset)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthExpense: StateFlow<Double> =
        combine(monthOffset, currentBookId) { offset, bookId ->
            Pair(offset, bookId)
        }.flatMapLatest { (offset, bookId) ->
            repository.getTotalAmountBetween(
                bookId,
                DateUtils.getMonthStart(offset),
                DateUtils.getMonthEnd(offset),
                0
            )
                .map { it ?: 0.0 }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthIncome: StateFlow<Double> =
        combine(monthOffset, currentBookId) { offset, bookId ->
            Pair(offset, bookId)
        }.flatMapLatest { (offset, bookId) ->
            repository.getTotalAmountBetween(
                bookId,
                DateUtils.getMonthStart(offset),
                DateUtils.getMonthEnd(offset),
                1
            )
                .map { it ?: 0.0 }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyBudget: StateFlow<Double> = monthOffset.flatMapLatest { offset ->
        val key = DateUtils.getYearMonthKey(offset)
        userPrefsRepository.getMonthlyBudget(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    fun changeMonth(delta: Int) {
        if (monthOffset.value + delta <= 0) {
            monthOffset.value += delta
        }
    }

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            val key = DateUtils.getYearMonthKey(monthOffset.value)
            userPrefsRepository.updateMonthlyBudget(key, newBudget)
        }
    }

    // ✨ 核心修改：新增 bookId 形参，保存账单时打上账本烙印
    fun addLedger(
        bookId: Long,
        amount: Double,
        type: Int,
        categoryName: String,
        categoryIcon: String,
        timestamp: Long,
        note: String
    ) {
        viewModelScope.launch {
            val newLedger = LedgerEntity(
                bookId = bookId, // ✨
                amount = amount,
                type = type,
                categoryName = categoryName,
                categoryIcon = categoryIcon,
                timestamp = timestamp,
                note = note,
                source = "MANUAL"
            )
            repository.insertLedger(newLedger)
        }
    }

    fun updateLedger(ledger: LedgerEntity) {
        viewModelScope.launch { repository.insertLedger(ledger) }
    }

    fun deleteLedger(ledger: LedgerEntity) {
        viewModelScope.launch { repository.deleteLedger(ledger) }
    }

    fun toggleSelection(ledgerId: Long) {
        val current = _selectedLedgerIds.value.toMutableSet()
        if (current.contains(ledgerId)) current.remove(ledgerId) else current.add(ledgerId)
        _selectedLedgerIds.value = current
    }

    fun selectAll(ledgerIds: List<Long>) {
        if (_selectedLedgerIds.value.size == ledgerIds.size) _selectedLedgerIds.value = emptySet()
        else _selectedLedgerIds.value = ledgerIds.toSet()
    }

    fun clearSelection() {
        _selectedLedgerIds.value = emptySet()
    }

    fun deleteSelectedLedgers() {
        val idsToDelete = _selectedLedgerIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            repository.deleteLedgersByIds(idsToDelete)
            clearSelection()
        }
    }
}