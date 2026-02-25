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
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题

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
        color = AppTheme.colors.cardBackground,
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
fun DataOverviewSection(stats: MonthlyStats, budget: Double) {
    val expense = stats.totalExpense.toDoubleOrNull() ?: 0.0
    val income = stats.totalIncome.toDoubleOrNull() ?: 0.0
    val balance = income - expense
    val balanceStr = String.format("%.2f", balance)

    val usageRate = if (budget > 0) (expense / budget).toFloat().coerceIn(0f, 1f) else 0f
    val usagePercent = (usageRate * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- 1. 头部标题 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            // ✨ 映射专属面板强调色
            Box(Modifier.size(3.dp, 14.dp).background(AppTheme.colors.overviewIndicatorColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            // ✨ 复用主文本色
            Text("数据总览", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }

        // --- 2. 核心支出区 (主数据) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                // ✨ 复用次要文本色
                Text("本月累计支出", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, end = 2.dp),
                        color = AppTheme.colors.textPrimary // ✨ 显式指定，防止深色模式变黑
                    )
                    Text(
                        stats.totalExpense,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AppTheme.colors.textPrimary // ✨ 显式指定
                    )
                }
            }
            // 右侧辅助数据：预算剩余
            Column(horizontalAlignment = Alignment.End) {
                Text("预算剩余", fontSize = 11.sp, color = AppTheme.colors.textSecondary) // ✨
                val remaining = (budget - expense).coerceAtLeast(0.0)
                Text(
                    "¥${String.format("%.0f", remaining)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary // ✨
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- 3. 可视化进度条 (高级感核心) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("预算使用进度", fontSize = 11.sp, color = AppTheme.colors.textSecondary) // ✨
                // ✨ 映射专属面板强调色
                Text("$usagePercent%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.overviewIndicatorColor)
            }
            Spacer(Modifier.height(8.dp))
            // 细长的进度条背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    // ✨ 复用表层灰底色
                    .background(AppTheme.colors.surfaceVariant)
            ) {
                // 渐变进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usageRate)
                        .fillMaxHeight()
                        .background(
                            // ✨ 完美复用我们为图表设计的渐变配置 (由于原图是从绿到蓝，这里反转组合即可)
                            brush = Brush.horizontalGradient(
                                listOf(AppTheme.colors.chartGradientEnd, AppTheme.colors.chartGradientStart)
                            )
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 4. 底部次要数据网格 (收入 & 结余) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                // ✨ 映射专属子面板底色
                .background(AppTheme.colors.overviewSubPanelBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ✨ 总收入数值色复用主文本色 (原 0xFF1D1D1F)
            DataMiniBox("总收入", "¥${stats.totalIncome}", AppTheme.colors.textPrimary)

            // ✨ 垂直分割线复用全局分割线颜色
            Box(Modifier.width(1.dp).height(20.dp).background(AppTheme.colors.dividerColor).align(Alignment.CenterVertically))

            // ✨ 结余颜色完美复用全局的收支语义色
            DataMiniBox(
                "本月结余",
                "¥$balanceStr",
                if (balance >= 0) AppTheme.colors.incomeColor else AppTheme.colors.expenseColor
            )
        }
    }
}

@Composable
fun DataMiniBox(label: String, value: String, color: Color) {
    Column {
        // ✨ 复用次要文本色
        Text(label, fontSize = 11.sp, color = AppTheme.colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}