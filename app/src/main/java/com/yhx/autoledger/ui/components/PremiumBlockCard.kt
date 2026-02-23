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

// ✨ 1. 统一的高级自适应白卡容器
@Composable
fun PremiumBlockCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp), // ✨ 减小垂直外边距
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        content = {
            Column(
                modifier = Modifier.padding(12.dp), // ✨ 核心修改：内部留白从 20dp 减到 12dp，解决“卡片太高”
                content = content
            )
        }
    )
}


// ✨ 定义你要求的指定渐变色
val ChartPremiumGradient = Brush.verticalGradient(
    listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))
)

@Composable
fun DataOverviewSection(stats: MonthlyStats, budget: Double) {
    val expense = stats.totalExpense.toDoubleOrNull() ?: 0.0
    val income = stats.totalIncome.toDoubleOrNull() ?: 0.0
    val balance = income - expense
    val balanceStr = String.format("%.2f", balance)

    // 计算预算使用率
    val usageRate = if (budget > 0) (expense / budget).toFloat().coerceIn(0f, 1f) else 0f
    val usagePercent = (usageRate * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- 1. 头部标题 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(Modifier.size(3.dp, 14.dp).background(Color(0xFF8FD3F4), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("数据总览", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }

        // --- 2. 核心支出区 (主数据) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("本月累计支出", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, end = 2.dp))
                    Text(stats.totalExpense, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
            }
            // 右侧辅助数据：预算剩余
            Column(horizontalAlignment = Alignment.End) {
                Text("预算剩余", fontSize = 11.sp, color = Color.Gray)
                val remaining = (budget - expense).coerceAtLeast(0.0)
                Text("¥${String.format("%.0f", remaining)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- 3. 可视化进度条 (高级感核心) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("预算使用进度", fontSize = 11.sp, color = Color.Gray)
                Text("$usagePercent%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8FD3F4))
            }
            Spacer(Modifier.height(8.dp))
            // 细长的进度条背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F6))
            ) {
                // 渐变进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usageRate)
                        .fillMaxHeight()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))
                            )
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 4. 底部次要数据网格 (收入 & 结余) ---
        // 使用带有微底色的横向面板，增加归属感
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F9FB))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DataMiniBox("总收入", "¥${stats.totalIncome}", Color(0xFF1D1D1F))

            // 垂直分割线
            Box(Modifier.width(1.dp).height(20.dp).background(Color(0xFFE5E7EB)).align(Alignment.CenterVertically))

            DataMiniBox(
                "本月结余",
                "¥$balanceStr",
                if (balance >= 0) Color(0xFF5CA969) else Color(0xFFD66969)
            )
        }
    }
}

@Composable
fun DataMiniBox(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

