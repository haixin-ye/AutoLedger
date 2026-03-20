package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.theme.AppDesignSystem // ✨ 引入全局主题

// ✨ 1. 统一的高级自适应白卡容器
@Composable
fun PremiumBlockCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        // ✨ 复用全局卡片背景色
        color = AppDesignSystem.colors.cardBackground,
        shadowElevation = 2.dp,
        content = {
            Column(
                modifier = Modifier.padding(12.dp),
                content = content
            )
        }
    )
}

// ⚠️ 注意：删除了之前写死的 val ChartPremiumGradient，彻底消除静态变量的副作用！

@Composable
fun DataOverviewSection(stats: MonthlyStats, budget: Double, isYearView: Boolean = false) {
    // ✨ 提前提取颜色变量，彻底解决 @Composable invocation 报错
    val textPrimary = AppDesignSystem.colors.textPrimary
    val textSecondary = AppDesignSystem.colors.textSecondary
    val indicatorColor = AppDesignSystem.colors.overviewIndicatorColor
    val surfaceVariant = AppDesignSystem.colors.surfaceVariant
    val dividerColor = AppDesignSystem.colors.dividerColor
    val subPanelBg = AppDesignSystem.colors.overviewSubPanelBg
    val incomeColor = AppDesignSystem.colors.incomeColor
    val expenseColor = AppDesignSystem.colors.expenseColor
    val gradientEnd = AppDesignSystem.colors.chartGradientEnd
    val gradientStart = AppDesignSystem.colors.chartGradientStart

    val expense = stats.totalExpense.toDoubleOrNull() ?: 0.0
    val income = stats.totalIncome.toDoubleOrNull() ?: 0.0
    val balance = income - expense
    val balanceStr = String.format("%.2f", balance)

    // ✨ 修复：计算真实占比，并格式化为 1 位小数的百分比字符串（如 "5.6%"）
    val usageRate = if (budget > 0) (expense / budget).toFloat().coerceIn(0f, 1f) else 0f
    val usagePercentStr = String.format("%.1f%%", usageRate * 100)

    // ✨ 动态前缀
    val prefix = if (isYearView) "本年" else "本月"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
            Box(Modifier.size(3.dp, 14.dp).background(indicatorColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("数据总览", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("${prefix}累计支出", fontSize = 12.sp, color = textSecondary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, end = 2.dp), color = textPrimary)
                    Text(stats.totalExpense, fontSize = 32.sp, fontWeight = FontWeight.Black, color = textPrimary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${prefix}预算剩余", fontSize = 11.sp, color = textSecondary)
                val remaining = (budget - expense).coerceAtLeast(0.0)
                Text("¥${String.format("%.0f", remaining)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("预算使用进度", fontSize = 11.sp, color = textSecondary)
                // ✨ 显示修复后的精准百分比
                Text(usagePercentStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = indicatorColor)
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(surfaceVariant)) {
                Box(modifier = Modifier.fillMaxWidth(usageRate).fillMaxHeight().background(
                    brush = Brush.horizontalGradient(listOf(gradientEnd, gradientStart))
                ))
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(subPanelBg).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DataMiniBox("总收入", "¥${stats.totalIncome}", textPrimary)
            Box(Modifier.width(1.dp).height(20.dp).background(dividerColor).align(Alignment.CenterVertically))
            DataMiniBox("${prefix}结余", "¥$balanceStr", if (balance >= 0) incomeColor else expenseColor)
        }
    }
}

@Composable
fun DataMiniBox(label: String, value: String, color: Color) {
    Column {
        // ✨ 复用次要文本色
        Text(label, fontSize = 11.sp, color = AppDesignSystem.colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}