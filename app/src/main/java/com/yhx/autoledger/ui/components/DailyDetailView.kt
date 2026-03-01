package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.ui.theme.CategoryFood
import com.yhx.autoledger.ui.theme.CategoryOther
import com.yhx.autoledger.ui.theme.CategoryShop
import com.yhx.autoledger.ui.theme.CategoryTransport
import java.time.YearMonth
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import androidx.activity.compose.BackHandler

// ✨ 新增组件：每日详细账单列表
@Composable
fun DailyDetailView(
    day: Int,
    month: YearMonth,
    allLedgers: List<LedgerEntity>,
    onBack: () -> Unit,
    onSaveLedger: (LedgerEntity) -> Unit,
    onDeleteLedger: (LedgerEntity) -> Unit
) {

    // ✨ 新增：拦截系统的返回键和侧滑返回手势，让它执行自定义的回退逻辑
    BackHandler {
        onBack()
    }
    val dayLedgers = remember(allLedgers, day, month) {
        allLedgers.filter { ledger ->
            val cal = Calendar.getInstance().apply { timeInMillis = ledger.timestamp }
            cal.get(Calendar.DAY_OF_MONTH) == day &&
                    cal.get(Calendar.MONTH) + 1 == month.monthValue &&
                    cal.get(Calendar.YEAR) == month.year
        }.sortedByDescending { it.timestamp }
    }

    var selectedLedger by remember { mutableStateOf<LedgerEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDesignSystem.colors.appBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowLeft,
                    contentDescription = "返回",
                    tint = AppDesignSystem.colors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "${month.monthValue}月${day}日 账单明细",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppDesignSystem.colors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (dayLedgers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("这一天没有记账哦 🍃", color = AppDesignSystem.colors.textSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dayLedgers) { ledger ->
                    val displayData = remember(ledger) {
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

                    RefinedTransactionItem(
                        data = displayData,
                        onClick = { selectedLedger = ledger }
                    )
                }
            }
        }
    }

    if (selectedLedger != null) {
        EditLedgerSheet(
            initialLedger = selectedLedger!!,
            onDismiss = { selectedLedger = null },
            onSave = {
                onSaveLedger(it)
                selectedLedger = null
            },
            onDelete = {
                onDeleteLedger(it)
                selectedLedger = null
            }
        )
    }
}