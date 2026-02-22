package com.yhx.autoledger.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SelectAll
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
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
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
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
            ) {
                // ✨ 升级版：深色高级双功能控制胶囊
                val isAllSelected =
                    recentLedgers.isNotEmpty() && selectedIds.size == recentLedgers.size

                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF2C2C2E), // 苹果风高级深灰
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：全选/取消全选区
                        Row(
                            modifier = Modifier
                                .clickable(
                                    // 移除点击涟漪，或者你可以用 bounceClick
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.selectAll(recentLedgers.map { it.id })
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "全选",
                                tint = if (isAllSelected) Color(0xFF8FD3F4) else Color.White, // 全选后图标变色反馈
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isAllSelected) "取消" else "全选",
                                color = if (isAllSelected) Color(0xFF8FD3F4) else Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }

                        // 中间：柔和的分割线
                        Spacer(Modifier.width(16.dp))
                        Box(Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF48484A)))
                        Spacer(Modifier.width(16.dp))

                        // 右侧：红色删除区
                        androidx.compose.material3.Button(
                            onClick = { viewModel.deleteSelectedLedgers() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF3B30)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 0.dp
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "删除",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "删除 ${selectedIds.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // ✨ 修复 2：手势拦截。点击任何非账单卡片的空白处，直接清空选择！
                .pointerInput(isSelectionMode) {
                    if (isSelectionMode) {
                        detectTapGestures(
                            onTap = {
                                viewModel.clearSelection()
                            }
                        )
                    }
                }
                .padding(paddingValues)
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
                        // ✨ 细节优化：如果在多选模式下点到了资产卡片，也是执行取消多选，而不是弹预算设置
                        if (isSelectionMode) {
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
                        // ✨ 动态计算该日期的总支出和总收入
                        val dailyExpense = items.filter { it.originalLedger?.type == 0 }
                            .sumOf { Math.abs(it.originalLedger?.amount ?: 0.0) }
                        val dailyIncome = items.filter { it.originalLedger?.type == 1 }
                            .sumOf { Math.abs(it.originalLedger?.amount ?: 0.0) }

                        // ✨ 使用 Row 布局，将日期放左边，统计放右边
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom // 底部对齐，视觉更平稳
                        ) {
                            // 左侧：日期与星期
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )

                            // 右侧：收支汇总
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
//                                        color = Color(0xFF5CA969) // 🌿 柔和自然的草绿色
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
//                                        color = Color(0xFFD66969) // 🥀 优雅不刺眼的豆沙红
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                    items(items) { data ->
                        val ledgerId = data.originalLedger?.id ?: -1L
                        val isSelected = selectedIds.contains(ledgerId)

                        RefinedTransactionItem(
                            data = data,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode, // ✨ 修复 1：补上致命的参数传递！
                            onLongClick = {
                                if (ledgerId != -1L) {
                                    viewModel.toggleSelection(ledgerId)
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