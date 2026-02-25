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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush // ✨ 新增 Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题

@Composable
fun MainBalanceCard(
    expense: String, budget: String, income: String, balance: String, dailyAvg: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(230.dp)
            .bounceClick(),
        shape = RoundedCornerShape(32.dp),
        // ✨ 给卡片容器增加默认背景映射，防止边缘漏色
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // ✨ 动态组装主题配置的渐变色
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
                .background(dynamicGradient) // ✨ 使用动态渐变替换 LightBlueGradient
        ) {
            // 1. 背景装饰圆（放在底层）
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 220.dp, y = (-40).dp),
                color = AppTheme.colors.balanceCardCircleDecoration, // ✨ 替换硬编码透明白
                shape = CircleShape
            ) {}

            // 2. 内容层（确保在最表层）
            Column(modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "本月支出",
                            color = AppTheme.colors.balanceCardTextSecondary, // ✨ 替换 DarkGray
                            fontSize = 14.sp
                        )
                        Text(
                            "¥ $expense",
                            color = AppTheme.colors.balanceCardTextPrimary, // ✨ 替换 Black
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    // 日均展示区
                    Surface(
                        color = AppTheme.colors.balanceCardDailyAvgBg, // ✨ 替换 White 60%
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                "日均可用",
                                color = AppTheme.colors.balanceCardDailyAvgText, // ✨ 替换 AccentBlue
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "¥ $dailyAvg",
                                color = AppTheme.colors.balanceCardTextPrimary, // ✨ 替换 Black
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 三列数据
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardDetailItem("总预算", budget)
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
            color = AppTheme.colors.balanceCardTextSecondary, // ✨ 替换 DarkGray 50%
            fontSize = 13.sp
        )
        Text(
            "¥$value",
            color = AppTheme.colors.balanceCardTextPrimary, // ✨ 替换 Black
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}