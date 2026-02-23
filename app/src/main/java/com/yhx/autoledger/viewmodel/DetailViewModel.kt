package com.yhx.autoledger.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Calendar
import javax.inject.Inject


// 💡 新增：用于日历格子显示每日收支
data class DailyRecord(
    val expense: Double = 0.0,
    val income: Double = 0.0
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    /// ✨ 计算当前月份的 Key (例如 "202405")
    private val currentYearMonthKey: Flow<String> = snapshotFlow { monthOffset.value }
        .map { offset ->
            val date = YearMonth.now().plusMonths(offset.toLong())
            "${date.year}${String.format("%02d", date.monthValue)}"
        }

    // ✨ 核心：根据月份 Key 动态获取真实预算
    val monthlyBudget: StateFlow<Double> = currentYearMonthKey
        .flatMapLatest { key -> userPrefs.getMonthlyBudget(key) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    // ✨ 核心状态：月份偏移量（与 HomeViewModel 保持一致）
    val monthOffset = MutableStateFlow(0)

    // ================== 1. 基础数据流 ==================

    // 当月总支出
    private val totalExpenseFlow = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), 0)
            .map { it ?: 0.0 }
    }

    // 当月总收入
    private val totalIncomeFlow = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), 1)
            .map { it ?: 0.0 }
    }

    // ================== 2. UI 绑定的衍生状态 ==================

    // 📊 数据总览 (整合支出、收入、结余、日均)
    val monthlyStats: StateFlow<MonthlyStats> = combine(
        totalExpenseFlow,
        totalIncomeFlow,
        monthOffset
    ) { expense, income, offset ->
        val balance = income - expense
        // 获取天数信息：如果是本月则除以“今天到了第几天”，如果是历史月份除以“该月总天数”
        val (passedDays, _) = DateUtils.getDaysInfo(offset)
        val dailyAvg = if (passedDays > 0) expense / passedDays else 0.0

        MonthlyStats(
            totalExpense = String.format("%.2f", expense),
            totalIncome = String.format("%.2f", income),
            balance = String.format("%.2f", balance),
            dailyAvg = String.format("%.2f", dailyAvg)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyStats("0.00", "0.00", "0.00", "0.00")
    )

    // 📅 日历网格每日汇总：Map<日期, 当天总支出>
    // ✨ 同时统计每天的收入和支出
    val dailyRecordsMap: StateFlow<Map<Int, DailyRecord>> = monthOffset.flatMapLatest { offset ->
        repository.getLedgersBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset))
            .map { ledgers ->
                ledgers.groupBy { ledger ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = ledger.timestamp }
                    calendar.get(Calendar.DAY_OF_MONTH)
                }.mapValues { entry ->
                    val dayExpense = entry.value.filter { it.type == 0 }.sumOf { it.amount }
                    val dayIncome = entry.value.filter { it.type == 1 }.sumOf { it.amount }
                    DailyRecord(expense = dayExpense, income = dayIncome)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // 🥧 分类饼图与列表数据
    val categoryPercentages: StateFlow<List<CategoryPercentage>> = monthOffset.flatMapLatest { offset ->
        repository.getCategorySumBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), type = 0)
            .map { categorySums ->
                val totalAmount = categorySums.sumOf { it.totalAmount }
                if (totalAmount == 0.0) return@map emptyList()

                categorySums.mapIndexed { index, sum ->
                    CategoryPercentage(
                        name = sum.categoryName,
                        amount = String.format("%.2f", sum.totalAmount),
                        percentage = (sum.totalAmount / totalAmount).toFloat(),
                        icon = sum.categoryIcon ?: "🏷️",
                        color = getPremiumChartColor(index)
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 📜 供下钻到二级页面的分类流水列表
    val currentMonthLedgers: StateFlow<List<LedgerEntity>> = monthOffset.flatMapLatest { offset ->
        repository.getLedgersBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ================== 辅助方法 ==================

    // ✨ 高级感色彩美学：莫兰迪 + 现代扁平色系
    private fun getPremiumChartColor(index: Int): Color {
        val colors = listOf(
            Color(0xFF5C6BC0), // Indigo
            Color(0xFF26A69A), // Teal
            Color(0xFFFF7043), // Deep Orange
            Color(0xFF42A5F5), // Blue
            Color(0xFFAB47BC), // Purple
            Color(0xFFFFCA28), // Amber
            Color(0xFFEC407A), // Pink
            Color(0xFF9CCC65), // Light Green
            Color(0xFF26C6DA), // Cyan
            Color(0xFF8D6E63)  // Brown
        )
        return colors[index % colors.size]
    }

    // ✨ 更新账单
    fun updateLedger(ledger: LedgerEntity) {
        viewModelScope.launch {
            repository.updateLedger(ledger)
        }
    }

    // ✨ 删除账单
    fun deleteLedger(ledger: LedgerEntity) {
        viewModelScope.launch {
            repository.deleteLedger(ledger)
        }
    }
}