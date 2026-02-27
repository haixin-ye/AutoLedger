package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.viewmodel.MonthlyRecord
import kotlin.math.abs

@Composable
fun YearlyCalendarGrid(
    year: Int,
    monthlyMap: Map<Int, MonthlyRecord>,
    onMonthClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxAbsBalance = monthlyMap.values.maxOfOrNull { abs(it.income - it.expense) }?.takeIf { it > 0.0 } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (rowIndex in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0 until 6) {
                    val month = rowIndex * 6 + colIndex + 1
                    val record = monthlyMap[month] ?: MonthlyRecord()
                    val net = record.income - record.expense

                    val hasData = record.income > 0 || record.expense > 0
                    val intensity = (abs(net) / maxAbsBalance).toFloat().coerceIn(0f, 1f)

                    YearlyMonthCell(
                        month = month,
                        net = net,
                        hasData = hasData,
                        intensity = intensity,
                        modifier = Modifier.weight(1f),
                        onClick = { onMonthClick(month) } // ✨ 核心修改 1：把月份传出去
                    )
                }
            }
        }
    }
}

@Composable
fun YearlyMonthCell(
    month: Int,
    net: Double,
    hasData: Boolean,
    intensity: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // ✨ 核心修改 2：新增点击事件参数
) {
    // 1. 取出专属的热力配置色
    val surplusBase = AppDesignSystem.colors.heatmapSurplusBase
    val surplusActive = AppDesignSystem.colors.heatmapSurplusActive
    val deficitBase = AppDesignSystem.colors.heatmapDeficitBase
    val deficitActive = AppDesignSystem.colors.heatmapDeficitActive
    val textPrimary = AppDesignSystem.colors.heatmapTextPrimary
    val textSecondary = AppDesignSystem.colors.heatmapTextSecondary
    val textOnActive = AppDesignSystem.colors.heatmapTextOnActive
    val emptyBg = AppDesignSystem.colors.surfaceVariant

    // 2. 极简的颜色插值逻辑：在 Base 和 Active 之间直接算色
    val bgColor = if (!hasData) {
        emptyBg
    } else {
        if (net >= 0) {
            lerp(surplusBase, surplusActive, intensity)
        } else {
            lerp(deficitBase, deficitActive, intensity)
        }
    }

    // 3. 根据浓度决定是否反白文字，保证绝佳对比度
    val useActiveText = intensity > 0.4f
    val titleColor = if (hasData && useActiveText) textOnActive else if (hasData) textPrimary else textSecondary
    val amountColor = if (useActiveText) textOnActive else textSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(10.dp)) // 必须放在 clickable 前面限制水波纹形状
            .clickable { onClick() }         // ✨ 核心修改 3：赋予点击反馈
            .background(bgColor)
            .padding(2.dp)
    ) {
        Text(
            text = "${month}月",
            fontSize = 12.sp,
            fontWeight = if (hasData) FontWeight.Bold else FontWeight.Medium,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (hasData) {
            val prefix = if (net >= 0) "+" else ""
            Text(
                text = "$prefix${net.toInt()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text("-", fontSize = 10.sp, color = AppDesignSystem.colors.textTertiary)
        }
    }
}