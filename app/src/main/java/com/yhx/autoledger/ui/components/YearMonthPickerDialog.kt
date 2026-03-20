package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yhx.autoledger.ui.theme.AppDesignSystem // ✨ 引入全局主题
import java.time.YearMonth

@Composable
fun YearMonthPickerDialog(
    initialMonth: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialMonth.monthValue) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            // ✨ 复用全局卡片/弹窗背景色
            color = AppDesignSystem.colors.cardBackground,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 年份切换 Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        // ✨ 显式指定图标颜色，适配深色模式
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一年", tint = AppDesignSystem.colors.textPrimary)
                    }
                    Text(
                        text = "$selectedYear 年",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        // ✨ 复用主文本色
                        color = AppDesignSystem.colors.textPrimary
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        // ✨ 显式指定图标颜色
                        Icon(Icons.Default.ChevronRight, contentDescription = "下一年", tint = AppDesignSystem.colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 12个月份网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = month == selectedMonth

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                // ✨ 选中态用品牌色，未选中用表层灰槽色
                                .background(if (isSelected) AppDesignSystem.colors.brandAccent else AppDesignSystem.colors.surfaceVariant)
                                .clickable { selectedMonth = month }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${month}月",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                // ✨ 选中时用白色(OnAccent)，未选中时用主文本色
                                color = if (isSelected) AppDesignSystem.colors.textOnAccent else AppDesignSystem.colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 底部操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        // ✨ 复用次要文本色
                        Text("取消", color = AppDesignSystem.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) },
                        // ✨ 复用品牌色
                        colors = ButtonDefaults.buttonColors(containerColor = AppDesignSystem.colors.brandAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        // ✨ 显式指定白色
                        Text("确定", color = AppDesignSystem.colors.textOnAccent)
                    }
                }
            }
        }
    }
}