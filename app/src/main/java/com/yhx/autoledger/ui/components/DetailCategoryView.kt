package com.yhx.autoledger.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.models.CategoryPercentage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CategoryDetailView(
    category: CategoryPercentage,
    categoryIndex: Int,
    allLedgers: List<LedgerEntity>,
    onBack: () -> Unit,
    onSaveLedger: (LedgerEntity) -> Unit,   // ✨ 接收保存回调
    onDeleteLedger: (LedgerEntity) -> Unit  // ✨ 接收删除回调
) {



    BackHandler {
        onBack() // 执行返回逻辑（即清空 DetailScreen 中的 selectedCategoryInfo）
    }
    val categoryLedgers = remember(category, allLedgers) {
        allLedgers.filter { it.categoryName == category.name }
            .sortedByDescending { it.timestamp }
    }

    val themeColor = getPremiumBaseColor(categoryIndex)
    var ledgerToEdit by remember { mutableStateOf<LedgerEntity?>(null) }

    Column(Modifier
        .fillMaxSize()
        .background(Color(0xFFF7F9FC))) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text("${category.name} 明细", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("共 ${categoryLedgers.size} 笔", fontSize = 13.sp, color = Color.Gray)
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            items(categoryLedgers) { ledger ->
                DetailedTransactionItem(
                    ledger = ledger,
                    themeColor = themeColor,
                    onClick = { ledgerToEdit = ledger } // 点击唤起编辑
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // ✨ 完美挂载您的 EditLedgerSheet
    ledgerToEdit?.let { ledger ->
        EditLedgerSheet(
            initialLedger = ledger,
            onDismiss = { ledgerToEdit = null },
            onSave = { updatedLedger ->
                onSaveLedger(updatedLedger) // 保存到数据库
                ledgerToEdit = null         // 关闭弹窗
            },
            onDelete = { deletedLedger ->
                onDeleteLedger(deletedLedger) // 从数据库删除
                ledgerToEdit = null           // 关闭弹窗
            }
        )
    }
}

@Composable
fun DetailedTransactionItem(ledger: LedgerEntity, themeColor: Color, onClick: () -> Unit) {
    // ✨ 精准控制时间格式：MM-dd 周X HH:mm
    val timeString = remember(ledger.timestamp) {
        val date = Date(ledger.timestamp)
        val monthDay = SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

        val calendar = Calendar.getInstance().apply { timeInMillis = ledger.timestamp }
        // Calendar.DAY_OF_WEEK 返回值 1~7 对应 周日~周六
        val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val weekDayStr = weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        "$monthDay $weekDayStr $time" // 拼装最终结果，例如：05-12 周三 18:30
    }

    // 读取备注信息
    val remarkStr = ledger.note ?: ""
    val displayTitle =
        if (remarkStr.isNotBlank() && remarkStr != ledger.categoryName) remarkStr else ledger.categoryName

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 屏蔽系统水波纹，保持高级手感
            ) { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(ledger.categoryIcon ?: "🏷️", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // ✨ 这里的时间显示变成了精准的中文星期
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Text(
                text = "- ¥${String.format("%.2f", ledger.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2D3436)
            )
        }
    }
}