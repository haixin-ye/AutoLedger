package com.yhx.autoledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.ui.components.BudgetSettingSheet
import com.yhx.autoledger.ui.components.DoubleCircleGauges
import com.yhx.autoledger.ui.components.EditLedgerSheet
import com.yhx.autoledger.ui.components.MainBalanceCard
import com.yhx.autoledger.ui.components.RefinedTransactionItem
import com.yhx.autoledger.ui.components.TransactionData
import com.yhx.autoledger.ui.theme.CategoryFood
import com.yhx.autoledger.ui.theme.CategoryOther
import com.yhx.autoledger.ui.theme.CategoryShop
import com.yhx.autoledger.ui.theme.CategoryTransport
import com.yhx.autoledger.utils.DateUtils
import com.yhx.autoledger.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val recentLedgers by viewModel.recentLedgers.collectAsState()
    val monthExpense by viewModel.currentMonthExpense.collectAsState()
    val monthIncome by viewModel.currentMonthIncome.collectAsState()
    val budget by viewModel.monthlyBudget.collectAsState()
    val monthOffset by viewModel.monthOffset.collectAsState()
    var ledgerToEdit by remember { mutableStateOf<LedgerEntity?>(null) }
    var showBudgetSheet by remember { mutableStateOf(false) }

    // ✨ 需求 3 & 4：精准的业务逻辑计算
    val balance = monthIncome - monthExpense
    val (currentDay, totalDays) = DateUtils.getDaysInfo(monthOffset)

    // 1. 获取“今天”的纯支出
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isCurrentMonth = (monthOffset == 0) // 判断是否在看本月
    val todayExpense = if (isCurrentMonth) {
        recentLedgers.filter {
            it.type == 0 && SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == todayStr
        }.sumOf { it.amount }
    } else { 0.0 }

    // 2. 获取截止到“昨天”的纯历史支出
    val historicalExpense = monthExpense - todayExpense

    // ✨ 3. 核心算法重构：
    // 本月剩余天数 (如果是历史月份，剩余天数算作1，防止除以0)
    val remainingDays = if (isCurrentMonth) (totalDays - currentDay + 1) else 1

    // 【今日限额标尺】(给圆环做分母用)：(总预算 - 昨天以前的支出) / 剩余天数
    // 如果预算调整了，这个标尺会立刻根据新预算重新计算！
    val todayAllowance = if (remainingDays > 0) (budget - historicalExpense) / remainingDays else 0.0

    // 【实时日均可用】(给顶部文字卡片用)：(总预算 - 今天及以前的所有支出) / 剩余天数
    val dailyAvailable = if (remainingDays > 0) (budget - monthExpense) / remainingDays else 0.0

    // 4. 计算圆环进度
    val monthProgress = if (budget > 0) (monthExpense / budget).toFloat() else 0f
    // 日进度 = 今天已花 / 今天的限额标尺
    val dayProgress = if (todayAllowance > 0) (todayExpense / todayAllowance).toFloat() else 0f

    // ✨ 需求 5 修复：转换数据并清理正负号显示逻辑
    val groupedRecords = remember(recentLedgers) {
        val dateFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINESE)
        recentLedgers.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { entry ->
                entry.value.map { ledger ->
                    val color = when (ledger.categoryName) {
                        "餐饮" -> CategoryFood
                        "交通" -> CategoryTransport
                        "购物" -> CategoryShop
                        else -> CategoryOther
                    }
                    // 修复 Bug：取绝对值，仅保留纯数字，把正负号和数字拼好（前提是 TransactionItem 不要再硬编码 ￥）
                    // 格式化出类似: "- 10.00" 或 "+ 8500.00"
                    val absVal = String.format(Locale.getDefault(), "%.2f", abs(ledger.amount))
// 拼接出优雅的格式，例如："- ¥1000.00"
                    val displayAmount = if (ledger.type == 0) "- ¥$absVal" else "+ ¥$absVal"

                    TransactionData(
                        title = ledger.note.ifBlank { ledger.categoryName },
                        icon = ledger.categoryIcon,
                        amount = displayAmount,
                        color = color,
                        originalLedger = ledger // ✨ 把原始数据塞进 UI 模型里
                    )
                }
            }.toList()
    }

    // 预算设置弹窗
    if (showBudgetSheet) {
        BudgetSettingSheet(
            currentBudget = budget,
            monthExpense = monthExpense,
            todayExpense=todayExpense,
            onDismiss = { showBudgetSheet = false },
            onSave = { newBudget -> viewModel.updateBudget(newBudget) }
        )
    }
    // 4. 在 Scaffold/Column 的某处（比如和 showBudgetSheet 放一起），挂载这个弹窗
    ledgerToEdit?.let { ledger ->
        EditLedgerSheet(
            initialLedger = ledger,
            onDismiss = { ledgerToEdit = null },
            onSave = { updatedLedger ->
                viewModel.updateLedger(updatedLedger)
            },
            onDelete = { ledgerToDelete ->
                viewModel.deleteLedger(ledgerToDelete)
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // ✨ 需求 1：月份切换栏
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                    Icon(Icons.Rounded.ChevronLeft, "上个月", tint = Color.Gray)
                }
                Text(
                    text = DateUtils.getYearMonthString(monthOffset),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black
                )
                IconButton(
                    onClick = { viewModel.changeMonth(1) },
                    // 如果已经是本月（offset==0），禁用下一月按钮
                    enabled = monthOffset < 0
                ) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        "下个月",
                        tint = if (monthOffset < 0) Color.Gray else Color.LightGray
                    )
                }
            }
        }

        // 终极资产卡片 (点击触发修改预算)
        item {
            MainBalanceCard(
                expense = String.format(Locale.getDefault(), "%.2f", monthExpense),
                dailyAvg = String.format(Locale.getDefault(), "%.2f", dailyAvailable),
                budget = String.format(Locale.getDefault(), "%.0f", budget),
                income = String.format(Locale.getDefault(), "%.2f", monthIncome),
                balance = String.format(Locale.getDefault(), "%.2f", balance),
                onClick = { showBudgetSheet = true } // ✨ 直接在这里触发弹窗！
            )

        }

        // 双圆形仪表盘 (传入真实进度和明细数据)
        item {
            DoubleCircleGauges(
                monthProgress = monthProgress,
                dayProgress = dayProgress,
                // ✨ 传给仪表盘底部的文字显示
                monthExpense = monthExpense,
                monthBudget = budget,
                dayExpense = todayExpense,
                dayBudget = todayAllowance
            )
        }

        // 账单列表
        if (groupedRecords.isEmpty()) {
            item {
                Text(
                    "该月暂无账单记录",
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            groupedRecords.forEach { (date, items) ->
                item {
                    Text(
                        text = date,
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                }
                items(items) { data ->
                    RefinedTransactionItem(
                        data = data,
                        onClick = { ledgerToEdit = data.originalLedger }
                    )

                }
            }
        }
    }
}