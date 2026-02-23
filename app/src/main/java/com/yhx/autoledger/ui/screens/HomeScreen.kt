package com.yhx.autoledger.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.ui.components.BatchDeleteBotton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val recentLedgers by viewModel.recentLedgers.collectAsState()
    val monthExpense by viewModel.currentMonthExpense.collectAsState()
    val monthIncome by viewModel.currentMonthIncome.collectAsState()
    val budget by viewModel.monthlyBudget.collectAsState()
    val monthOffset by viewModel.monthOffset.collectAsState()
    var ledgerToEdit by remember { mutableStateOf<LedgerEntity?>(null) }
    var showBudgetSheet by remember { mutableStateOf(false) }

    val selectedIds by viewModel.selectedLedgerIds.collectAsState()

    // ✨ 核心修改 1：将多选模式变成独立的状态，不再和 selectedIds 强绑定
    var isSelectionMode by remember { mutableStateOf(false) }

    // ✨ 核心修改 2：拦截返回键，手动退出多选模式并清空数据
    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        viewModel.clearSelection()
    }

    val balance = monthIncome - monthExpense
    val (currentDay, totalDays) = DateUtils.getDaysInfo(monthOffset)

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isCurrentMonth = (monthOffset == 0)
    val todayExpense = if (isCurrentMonth) {
        recentLedgers.filter {
            it.type == 0 && SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date(it.timestamp)) == todayStr
        }.sumOf { it.amount }
    } else {
        0.0
    }

    val historicalExpense = monthExpense - todayExpense
    val remainingDays = if (isCurrentMonth) (totalDays - currentDay + 1) else 1
    val todayAllowance =
        if (remainingDays > 0) (budget - historicalExpense) / remainingDays else 0.0
    val dailyAvailable = if (remainingDays > 0) (budget - monthExpense) / remainingDays else 0.0

    val monthProgress = if (budget > 0) (monthExpense / budget).toFloat() else 0f
    val dayProgress = if (todayAllowance > 0) (todayExpense / todayAllowance).toFloat() else 0f

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
                    val absVal = String.format(Locale.getDefault(), "%.2f", abs(ledger.amount))
                    val displayAmount = if (ledger.type == 0) "- ¥$absVal" else "+ ¥$absVal"

                    TransactionData(
                        title = ledger.note.ifBlank { ledger.categoryName },
                        icon = ledger.categoryIcon,
                        amount = displayAmount,
                        color = color,
                        originalLedger = ledger
                    )
                }
            }.toList()
    }

    if (showBudgetSheet) {
        BudgetSettingSheet(
            currentBudget = budget,
            monthExpense = monthExpense,
            todayExpense = todayExpense,
            onDismiss = { showBudgetSheet = false },
            onSave = { newBudget -> viewModel.updateBudget(newBudget) }
        )
    }

    ledgerToEdit?.let { ledger ->
        EditLedgerSheet(
            initialLedger = ledger,
            onDismiss = { ledgerToEdit = null },
            onSave = { updatedLedger -> viewModel.updateLedger(updatedLedger) },
            onDelete = { ledgerToDelete -> viewModel.deleteLedger(ledgerToDelete) }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            val isAllSelected = recentLedgers.isNotEmpty() && selectedIds.size == recentLedgers.size

            // 引入我们刚刚抽取的胶囊组件
            BatchDeleteBotton(
                isVisible = isSelectionMode, // 由独立状态控制
                isAllSelected = isAllSelected,
                selectedCount = selectedIds.size,
                onSelectAllToggle = {
                    // 全选/取消全选的逻辑在 ViewModel 中，执行后不会改变 isSelectionMode
                    viewModel.selectAll(recentLedgers.map { it.id })
                },
                onDeleteClick = {
                    // ✨ 只有在选中数量大于 0 时才执行删除
                    if (selectedIds.isNotEmpty()) {
                        viewModel.deleteSelectedLedgers()
                        isSelectionMode = false // 删除完毕后，优雅退出多选模式
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // ✨ 注意：这里删除了原来的 .padding(paddingValues)
                .pointerInput(isSelectionMode) {
                    if (isSelectionMode) {
                        detectTapGestures(
                            onTap = {
                                isSelectionMode = false
                                viewModel.clearSelection()
                            }
                        )
                    }
                },
            // ✨ 核心修复：使用 contentPadding 动态增加底部留白
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                // 如果处于多选模式，底部额外增加 88.dp 的空间（刚好是一个胶囊的高度 + 呼吸空间）
                // 正常模式下保留基础的 24.dp 留白，让页面底部不至于太拥挤
                bottom = paddingValues.calculateBottomPadding() + if (isSelectionMode) 88.dp else 24.dp
            )
        ) {

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

            item {
                MainBalanceCard(
                    expense = String.format(Locale.getDefault(), "%.2f", monthExpense),
                    dailyAvg = String.format(Locale.getDefault(), "%.2f", dailyAvailable),
                    budget = String.format(Locale.getDefault(), "%.0f", budget),
                    income = String.format(Locale.getDefault(), "%.2f", monthIncome),
                    balance = String.format(Locale.getDefault(), "%.2f", balance),
                    onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            viewModel.clearSelection()
                        } else {
                            showBudgetSheet = true
                        }
                    }
                )
            }

            item {
                DoubleCircleGauges(
                    monthProgress = monthProgress,
                    dayProgress = dayProgress,
                    monthExpense = monthExpense,
                    monthBudget = budget,
                    dayExpense = todayExpense,
                    dayBudget = todayAllowance
                )
            }

            if (groupedRecords.isEmpty()) {
                item {
                    Text(
                        "该月暂无账单记录",
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                groupedRecords.forEach { (date, items) ->
                    item {
                        // 包含收支统计的日期头部
                        val dailyExpense = items.filter { it.originalLedger?.type == 0 }
                            .sumOf { Math.abs(it.originalLedger?.amount ?: 0.0) }
                        val dailyIncome = items.filter { it.originalLedger?.type == 1 }
                            .sumOf { Math.abs(it.originalLedger?.amount ?: 0.0) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (dailyIncome > 0) {
                                    Text(
                                        text = "收 ¥${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.2f",
                                                dailyIncome
                                            )
                                        }",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color(0xFF5CA969)
                                        color = Color.Black
                                    )
                                }
                                if (dailyExpense > 0) {
                                    Text(
                                        text = "支 ¥${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.2f",
                                                dailyExpense
                                            )
                                        }",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color(0xFFD66969)
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                    items(
                        items = items,
                        key = { data -> data.originalLedger?.id ?: data.hashCode() } // ✨ 补充 key
                    ) { data ->
                        val ledgerId = data.originalLedger?.id ?: -1L
                        val isSelected = selectedIds.contains(ledgerId)

                        RefinedTransactionItem(
                            data = data,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onLongClick = {
                                if (ledgerId != -1L) {
                                    isSelectionMode = true // ✨ 核心修改 4：长按强制进入多选模式

                                    // 确保长按的条目被选中（避免原本选中时被反选）
                                    if (!selectedIds.contains(ledgerId)) {
                                        viewModel.toggleSelection(ledgerId)
                                    }
                                }
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    if (ledgerId != -1L) {
                                        viewModel.toggleSelection(ledgerId)
                                    }
                                } else {
                                    ledgerToEdit = data.originalLedger
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}