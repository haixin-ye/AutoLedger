package com.yhx.autoledger.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdvancedCategoryChart(data: List<Pair<String, Float>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(150.dp)) {
            var startAngle = -90f
            data.forEachIndexed { index, pair ->
                val sweepAngle = pair.second * 360f
                // 绘制带阴影效果的圆环段
                drawArc(
                    color = getCategoryColor(index).copy(alpha = 0.8f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 25.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
                startAngle += sweepAngle
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("总支出", fontSize = 12.sp, color = Color.Gray)
            Text("¥3250", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

fun getCategoryColor(index: Int) = when(index % 4) {
    0 -> Color(0xFF4FACFE)
    1 -> Color(0xFF00F2FE)
    2 -> Color(0xFF74EBD5)
    else -> Color(0xFF9FACE6)
}