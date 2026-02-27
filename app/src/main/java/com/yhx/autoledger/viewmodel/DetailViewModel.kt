package com.yhx.autoledger.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DailyRecord(val expense: Double = 0.0, val income: Double = 0.0)
data class MonthlyRecord(val expense: Double = 0.0, val income: Double = 0.0)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    // =========================================================================
    //  🔴 月视图状态管理
    // =========================================================================
    val monthOffset = MutableStateFlow(0)
    private val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val monthlyBudget: StateFlow<Double> = monthOffset.flatMapLatest { offset ->
        val key = DateUtils.getYearMonthKey(offset)
        userPrefs.getMonthlyBudget(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    private val totalExpenseFlow = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), 0).map { it ?: 0.0 }
    }

    private val totalIncomeFlow = monthOffset.flatMapLatest { offset ->
        repository.getTotalAmountBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), 1).map { it ?: 0.0 }
    }

    val monthlyStats: StateFlow<MonthlyStats> = combine(
        totalExpenseFlow, totalIncomeFlow, monthOffset
    ) { expense, income, offset ->
        val balance = income - expense
        val (passedDays, _) = DateUtils.getDaysInfo(offset)
        val dailyAvg = if (passedDays > 0) expense / passedDays else 0.0

        MonthlyStats(
            totalExpense = String.format("%.2f", expense),
            totalIncome = String.format("%.2f", income),
            balance = String.format("%.2f", balance),
            dailyAvg = String.format("%.2f", dailyAvg)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyStats("0.00", "0.00", "0.00", "0.00"))

    val dailyRecordsMap: StateFlow<Map<Int, DailyRecord>> = monthOffset.flatMapLatest { offset ->
        repository.getLedgersBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset))
            .map { ledgers ->
                ledgers.groupBy { ledger ->
                    Calendar.getInstance().apply { timeInMillis = ledger.timestamp }.get(Calendar.DAY_OF_MONTH)
                }.mapValues { entry ->
                    val dayExpense = entry.value.filter { it.type == 0 }.sumOf { it.amount }
                    val dayIncome = entry.value.filter { it.type == 1 }.sumOf { it.amount }
                    DailyRecord(expense = dayExpense, income = dayIncome)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val yearlyMonthlyRecordsMap: StateFlow<Map<Int, MonthlyRecord>> = selectedYear.flatMapLatest { year ->
        repository.getLedgersBetween(getYearStart(year), getYearEnd(year))
            .map { ledgers ->
                ledgers.groupBy { ledger ->
                    Calendar.getInstance().apply { timeInMillis = ledger.timestamp }.get(Calendar.MONTH) + 1
                }.mapValues { entry ->
                    val mExpense = entry.value.filter { it.type == 0 }.sumOf { it.amount }
                    val mIncome = entry.value.filter { it.type == 1 }.sumOf { it.amount }
                    MonthlyRecord(expense = mExpense, income = mIncome)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categoryPercentages: StateFlow<List<CategoryPercentage>> = monthOffset.flatMapLatest { offset ->
        repository.getCategorySumBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset), 0)
            .map { categorySums ->
                val totalAmount = categorySums.sumOf { it.totalAmount }
                if (totalAmount == 0.0) return@map emptyList()

                categorySums.map { sum ->
                    CategoryPercentage(
                        name = sum.categoryName,
                        amount = String.format("%.2f", sum.totalAmount),
                        percentage = (sum.totalAmount / totalAmount).toFloat(),
                        icon = sum.categoryIcon ?: "🏷️",
                        // ✨ 修复：不再处理颜色，赋透明值，UI 会通过 AppDesignSystem 自动上色
                        color = Color.Transparent
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthLedgers: StateFlow<List<LedgerEntity>> = monthOffset.flatMapLatest { offset ->
        repository.getLedgersBetween(DateUtils.getMonthStart(offset), DateUtils.getMonthEnd(offset))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // =========================================================================
    //  ☀️ 年视图状态管理
    // =========================================================================

    fun setYear(targetYear: Int) {
        selectedYear.value = targetYear
    }

    // ✨ 核心修复：真实年度预算 = 动态查询该年份 12 个月的独立预算之和
    val yearlyBudget: StateFlow<Double> = selectedYear.flatMapLatest { year ->
        val budgetFlows = (1..12).map { month ->
            userPrefs.getMonthlyBudget("${year}_${String.format("%02d", month)}")
        }
        combine(budgetFlows) { budgets -> budgets.sum() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val yearlyExpenseFlow = selectedYear.flatMapLatest { year ->
        repository.getTotalAmountBetween(getYearStart(year), getYearEnd(year), 0).map { it ?: 0.0 }
    }

    private val yearlyIncomeFlow = selectedYear.flatMapLatest { year ->
        repository.getTotalAmountBetween(getYearStart(year), getYearEnd(year), 1).map { it ?: 0.0 }
    }

    val yearlyStats: StateFlow<MonthlyStats> = combine(
        yearlyExpenseFlow, yearlyIncomeFlow, selectedYear
    ) { expense, income, year ->
        val balance = income - expense
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val passedDays = if (year == currentYear) calendar.get(Calendar.DAY_OF_YEAR)
        else if (year < currentYear) Calendar.getInstance().apply { set(Calendar.YEAR, year) }.getActualMaximum(Calendar.DAY_OF_YEAR)
        else 0

        val dailyAvg = if (passedDays > 0) expense / passedDays else 0.0

        MonthlyStats(
            totalExpense = String.format("%.2f", expense),
            totalIncome = String.format("%.2f", income),
            balance = String.format("%.2f", balance),
            dailyAvg = String.format("%.2f", dailyAvg)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyStats("0.00", "0.00", "0.00", "0.00"))

    val yearlyCategoryPercentages: StateFlow<List<CategoryPercentage>> = selectedYear.flatMapLatest { year ->
        repository.getCategorySumBetween(getYearStart(year), getYearEnd(year), 0).map { categorySums ->
            val totalAmount = categorySums.sumOf { it.totalAmount }
            if (totalAmount == 0.0) return@map emptyList()

            categorySums.map { sum ->
                CategoryPercentage(
                    name = sum.categoryName, amount = String.format("%.2f", sum.totalAmount),
                    percentage = (sum.totalAmount / totalAmount).toFloat(), icon = sum.categoryIcon ?: "🏷️",
                    color = Color.Transparent // 交给 UI 处理
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentYearLedgers: StateFlow<List<LedgerEntity>> = selectedYear.flatMapLatest { year ->
        repository.getLedgersBetween(getYearStart(year), getYearEnd(year))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🛠 辅助方法
    private fun getYearStart(year: Int): Long {
        return Calendar.getInstance().apply { clear(); set(Calendar.YEAR, year) }.timeInMillis
    }

    private fun getYearEnd(year: Int): Long {
        return Calendar.getInstance().apply { clear(); set(Calendar.YEAR, year); set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 31); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
    }

    fun updateLedger(ledger: LedgerEntity) { viewModelScope.launch { repository.updateLedger(ledger) } }
    fun deleteLedger(ledger: LedgerEntity) { viewModelScope.launch { repository.deleteLedger(ledger) } }
}