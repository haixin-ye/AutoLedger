package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MainBalanceCard(
    expense: String, budget: String, income: String, balance: String, dailyAvg: String,
    onClick: () -> Unit = {}
) {
    val expVal = expense.replace(",", "").toDoubleOrNull() ?: 0.0
    val budVal = budget.replace(",", "").toDoubleOrNull() ?: 0.0
    val remainVal = budVal - expVal
    val progress = if (budVal > 0) (expVal / budVal).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = remainVal < 0

    val formatter = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val remainStr = formatter.format(remainVal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(230.dp)
            .bounceClick(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // ✨ 稍微增加了阴影深度
    ) {
        val dynamicGradient = Brush.linearGradient(
            colors = listOf(
                AppTheme.colors.balanceCardGradientStart,
                AppTheme.colors.balanceCardGradientEnd
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .background(dynamicGradient)
        ) {
            // 背景装饰圆
            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = (-50).dp), // 移到左上角，换一种视觉平衡
                color = AppTheme.colors.balanceCardCircleDecoration,
                shape = CircleShape
            ) {}

            Column(modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()) {

                // 顶部：支出 & 剩余预算 Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "本月支出",
                        color = AppTheme.colors.balanceCardTextSecondary,
                        fontSize = 14.sp
                    )

                    // ✨ 剩余预算以 Tag 的形式展示在右上角
                    Surface(
                        color = if (isOverBudget) AppTheme.colors.warningRed.copy(alpha = 0.15f) else AppTheme.colors.balanceCardDailyAvgBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isOverBudget) "超支 ¥${formatter.format(-remainVal)}" else "剩余 ¥$remainStr",
                            color = if (isOverBudget) AppTheme.colors.warningRed else AppTheme.colors.balanceCardTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // 核心数字
                Text(
                    "¥ $expense",
                    color = AppTheme.colors.balanceCardTextPrimary,
                    fontSize = 42.sp, // 字体略微放大
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // 进度条
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isOverBudget) AppTheme.colors.warningRed else AppTheme.colors.balanceCardTextPrimary,
                    trackColor = AppTheme.colors.balanceCardCircleDecoration
                )

                Spacer(modifier = Modifier.weight(1f))

                // 底部四列精简排版
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardDetailItem("总预算", budget)
                    CardDetailItem("日均可用", dailyAvg) // 把日均放在这里对齐
                    CardDetailItem("总收入", income)
                    CardDetailItem("总结余", balance)
                }
            }
        }
    }
}

@Composable
fun CardDetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            color = AppTheme.colors.balanceCardTextSecondary,
            fontSize = 11.sp // 字体略微缩小以容纳4个元素
        )
        Text(
            value, // 取消前置的“¥”符号使排列更紧凑
            color = AppTheme.colors.balanceCardTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}