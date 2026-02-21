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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.ui.theme.LightBlueGradient

@Composable
fun MainBalanceCard(
    expense: String, budget: String, income: String, balance: String, dailyAvg: String,
    onClick: () -> Unit = {} // ✨ 1. 必须有这个参数
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(230.dp)
            // ⚠️ 外层不要加 clickable，只保留 bounceClick 动画
            .bounceClick(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // ✨ 2. 终极杀手锏：把点击事件加在最里层的 Box 上！
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() } // <-- 事件在这里触发，绝对不会失效
                .background(LightBlueGradient)
        ) {
            // 1. 背景装饰圆（放在底层）
            Surface(
                modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp),
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            ) {}

            // 2. 内容层（确保在最表层）
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("本月支出", color = Color.DarkGray.copy(alpha = 0.6f), fontSize = 14.sp)
                        Text("¥ $expense", color = Color.Black, fontSize = 38.sp, fontWeight = FontWeight.Black)
                    }
                    // 日均展示区
                    Surface(color = Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("日均可用", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("¥ $dailyAvg", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 三列数据
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        Text(label, color = Color.DarkGray.copy(alpha = 0.5f), fontSize = 13.sp)
        Text("¥$value", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}