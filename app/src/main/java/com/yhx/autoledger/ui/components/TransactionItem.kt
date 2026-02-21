package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.data.entity.LedgerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.getDefault

// 数据类
data class TransactionData(
    val title: String,
    val icon: String,
    val amount: String,
    val color: Color,
    val originalLedger: LedgerEntity? = null
)

// 共享的账单条目 UI 组件
@Composable
fun RefinedTransactionItem(
    data: TransactionData,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .bounceClick(), // 保留你的弹簧动画
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // ✨ 核心修复 1：在最内部的 Row 加上 .fillMaxWidth() 和 .clickable
        Row(
            modifier = Modifier
                .fillMaxWidth() // 确保可点击区域撑满整张卡片
                .clickable { onClick() } // ✨ 关键：把事件绑定在这里，避开冲突！
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标区域
            Box(
                Modifier
                    .size(48.dp)
                    .background(data.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(data.icon, fontSize = 22.sp)
            }

            Spacer(Modifier.width(16.dp))

            // 文本信息区域
            Column(Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, color = Color.Black)

                // ✨ 核心修复 2：把写死的 "12:30" 变成动态获取的真实时间
                val timeString = data.originalLedger?.let {
                    SimpleDateFormat("HH:mm", getDefault()).format(Date(it.timestamp))
                } ?: "12:00" // 兜底时间

                Text(timeString, fontSize = 12.sp, color = Color.Gray)
            }

            // 金额区域
            Text(
                text = data.amount,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.Black // 注意：如果前面设计了收入为绿色，也可以在这里根据 amount 包含 "+" 来动态变色
            )
        }
    }
}