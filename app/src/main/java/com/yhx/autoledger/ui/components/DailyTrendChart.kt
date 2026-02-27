package com.yhx.autoledger.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush // ✨ 新增
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb // ✨ 新增 toArgb()
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem // ✨ 引入全局主题
import com.yhx.autoledger.viewmodel.DailyRecord
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

enum class ChartStyle(val label: String) {
    BURNDOWN("消耗"), CURVE("曲线"), BAR("柱状")
}

@Composable
fun DailyTrendChart(month: YearMonth, dailyMap: Map<Int, DailyRecord>, budget: Double) {
    var selectedStyle by remember { mutableStateOf(ChartStyle.BURNDOWN) }

    val todayMonth = YearMonth.now()
    val displayDays = if (month == todayMonth) {
        LocalDate.now().dayOfMonth.coerceAtLeast(1)
    } else {
        month.lengthOfMonth()
    }

    val expenses = (1..displayDays).map { dailyMap[it]?.expense?.toFloat() ?: 0f }
    val maxExpense = expenses.maxOrNull()?.takeIf { it > 0 } ?: 100f

    val burnDownPoints = remember(dailyMap, budget, displayDays) {
        val points = mutableListOf<Float>()
        var current = budget.toFloat()
        for (e in expenses) {
            current -= e
            points.add(current)
        }
        points
    }

    var isDragging by remember { mutableStateOf(false) }
    var activeIndex by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    // ✨✨✨ 极其关键：在进入 Canvas 之前，提取所有需要的颜色变量！ ✨✨✨
    val textPrimaryColor = AppDesignSystem.colors.textPrimary
    val gridLineColor = AppDesignSystem.colors.chartGridLine
    val axisTextColor = AppDesignSystem.colors.chartAxisText.toArgb() // 转换为原生 Color Int
    val limitLineColor = AppDesignSystem.colors.warningRed.copy(alpha = 0.2f) // 复用全局警示红

    val tooltipLineColor = AppDesignSystem.colors.chartTooltipLine
    val tooltipOuterCircle = AppDesignSystem.colors.chartTooltipCircleOuter
    val tooltipBubbleBg = AppDesignSystem.colors.chartTooltipBubbleBg
    val tooltipBubbleText = AppDesignSystem.colors.chartTooltipBubbleText.toArgb() // 转换为原生 Color Int

    // ✨ 动态创建之前硬编码的 ChartPremiumGradient
    val dynamicChartGradient = Brush.verticalGradient(
        colors = listOf(AppDesignSystem.colors.chartGradientStart, AppDesignSystem.colors.chartGradientEnd)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- Header ---
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            // ✨ 复用主文字颜色
            Text("日消费趋势", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppDesignSystem.colors.chartToggleBg) // ✨ 专属色
                    .padding(2.dp)
            ) {
                listOf(ChartStyle.BURNDOWN, ChartStyle.CURVE, ChartStyle.BAR).forEach { style ->
                    val isSelected = selectedStyle == style
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AppDesignSystem.colors.chartToggleSelectedBg else Color.Transparent) // ✨ 专属色
                            .clickable { selectedStyle = style }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            style.label,
                            fontSize = 11.sp,
                            // ✨ 专属色
                            color = if (isSelected) AppDesignSystem.colors.chartToggleSelectedText else AppDesignSystem.colors.chartToggleUnselectedText
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 画布区域 ---
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val leftPadding = 100f
            val bottomPadding = 40f

            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(displayDays) {
                        val drawW = size.width - leftPadding
                        val stepX = if (displayDays > 1) drawW / (displayDays - 1) else drawW

                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                isDragging = true
                                val newIndex = ((offset.x - leftPadding) / stepX).roundToInt()
                                    .coerceIn(0, displayDays - 1)
                                activeIndex = newIndex
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val newIndex =
                                    ((change.position.x - leftPadding) / stepX).roundToInt()
                                        .coerceIn(0, displayDays - 1)
                                if (newIndex != activeIndex) {
                                    activeIndex = newIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false }
                        )
                    }
            ) {
                val drawW = size.width - leftPadding
                val drawH = size.height - bottomPadding
                val stepX = if (displayDays > 1) drawW / (displayDays - 1) else drawW
                val topVal =
                    if (selectedStyle == ChartStyle.BURNDOWN) budget.toFloat() else maxExpense

                // --- 绘制背景网格和 Y 轴 ---
                val segments = 4
                for (i in 0..segments) {
                    val ratio = i.toFloat() / segments
                    val yValueStr = if (i == segments) {
                        if (topVal % 1f == 0f) topVal.toInt().toString() else String.format("%.1f", topVal)
                    } else if (i == 0) {
                        "0"
                    } else {
                        val rawValue = topVal * ratio
                        val roundedValue = (Math.round(rawValue / 5f) * 5).toInt()
                        roundedValue.toString()
                    }

                    val yPos = drawH - (drawH - 20f) * ratio

                    if (i > 0) {
                        drawLine(
                            color = gridLineColor, // ✨ 使用提取的网格色
                            start = Offset(leftPadding, yPos),
                            end = Offset(size.width, yPos),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        yValueStr,
                        0f,
                        yPos + 10f,
                        Paint().apply { color = axisTextColor; textSize = 24f } // ✨ 使用原生转换色
                    )
                }

                // X 轴日期
                val xLabels = (1..displayDays step 7).toMutableList()
                if (!xLabels.contains(displayDays) && displayDays > 1) {
                    xLabels.add(displayDays)
                }
                xLabels.forEach { day ->
                    val x = leftPadding + (day - 1) * stepX
                    drawContext.canvas.nativeCanvas.drawText(
                        "${day}",
                        x,
                        size.height - 5f,
                        Paint().apply {
                            color = axisTextColor; textSize = 24f; textAlign = Paint.Align.CENTER // ✨ 使用原生转换色
                        }
                    )
                }

                // --- 绘制折线/柱状图主体 ---
                when (selectedStyle) {
                    ChartStyle.BURNDOWN -> {
                        val path = Path()
                        burnDownPoints.forEachIndexed { i, v ->
                            val x = leftPadding + i * stepX
                            val y = drawH - (v.coerceAtLeast(0f) / topVal) * (drawH - 20f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path,
                            brush = dynamicChartGradient, // ✨ 使用动态渐变色替代写死的 ChartPremiumGradient
                            style = Stroke(8f, cap = StrokeCap.Round)
                        )
                        drawLine(
                            limitLineColor, // ✨ 使用提取的底线色
                            Offset(leftPadding, drawH),
                            Offset(size.width, drawH),
                            2f
                        )
                    }
                    ChartStyle.CURVE -> {
                        val path = Path()
                        expenses.forEachIndexed { i, v ->
                            val x = leftPadding + i * stepX
                            val y = drawH - (v / topVal) * (drawH - 20f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path,
                            brush = dynamicChartGradient, // ✨
                            style = Stroke(6f, cap = StrokeCap.Round)
                        )
                    }
                    ChartStyle.BAR -> {
                        expenses.forEachIndexed { i, v ->
                            val barH = (v / topVal) * (drawH - 20f)
                            val barWidth = (stepX * 0.6f).coerceAtMost(40f)
                            drawRoundRect(
                                brush = dynamicChartGradient, // ✨
                                topLeft = Offset(
                                    leftPadding + i * stepX - barWidth / 2,
                                    drawH - barH
                                ),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(10f)
                            )
                        }
                    }
                }
            }

            // ✨ 高亮浮层
            val tooltipAlpha by animateFloatAsState(
                targetValue = if (isDragging) 1f else 0f,
                animationSpec = tween(durationMillis = 200)
            )

            if (tooltipAlpha > 0f) {
                Canvas(Modifier.fillMaxSize()) {
                    val drawW = size.width - leftPadding
                    val stepX = if (displayDays > 1) drawW / (displayDays - 1) else drawW
                    val topVal = if (selectedStyle == ChartStyle.BURNDOWN) budget.toFloat() else maxExpense

                    val targetX = leftPadding + activeIndex * stepX
                    val highlightVal = if (selectedStyle == ChartStyle.BURNDOWN) burnDownPoints[activeIndex] else expenses[activeIndex]
                    val targetY = (size.height - bottomPadding) - (highlightVal.coerceAtLeast(0f) / topVal) * ((size.height - bottomPadding) - 20f)

                    // 1. 垂直跟随线
                    drawLine(
                        color = tooltipLineColor.copy(alpha = 0.5f * tooltipAlpha), // ✨
                        start = Offset(targetX, 0f),
                        end = Offset(targetX, size.height - bottomPadding),
                        strokeWidth = 4f
                    )

                    // 2. 高亮圆点
                    drawCircle(
                        color = tooltipOuterCircle.copy(alpha = tooltipAlpha), // ✨
                        radius = 14f,
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = tooltipLineColor.copy(alpha = tooltipAlpha), // ✨ (内圆使用和跟随线同样的颜色)
                        radius = 8f,
                        center = Offset(targetX, targetY)
                    )

                    // 3. 气泡与文字
                    val labelText = if (selectedStyle == ChartStyle.BURNDOWN) "剩余" else "支出"
                    val valStr = if (highlightVal % 1 == 0f) highlightVal.toInt().toString() else String.format("%.1f", highlightVal)
                    val tooltipText = "${activeIndex + 1}日 $labelText: ¥$valStr"

                    val paint = Paint().apply {
                        textSize = 32f
                        color = tooltipBubbleText // ✨ 使用转换后的气泡文字色
                        alpha = (255 * tooltipAlpha).toInt()
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    val textBounds = android.graphics.Rect()
                    paint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)

                    val boxWidth = textBounds.width() + 40f
                    val boxHeight = textBounds.height() + 30f

                    var boxX = targetX
                    if (boxX - boxWidth / 2 < leftPadding) boxX = leftPadding + boxWidth / 2
                    if (boxX + boxWidth / 2 > size.width) boxX = size.width - boxWidth / 2

                    var boxY = targetY - 60f
                    if (boxY - boxHeight < 0) boxY = targetY + boxHeight + 30f

                    // 绘制气泡底图
                    drawRoundRect(
                        color = tooltipBubbleBg.copy(alpha = 0.9f * tooltipAlpha), // ✨
                        topLeft = Offset(boxX - boxWidth / 2, boxY - boxHeight),
                        size = Size(boxWidth, boxHeight),
                        cornerRadius = CornerRadius(16f)
                    )

                    // 绘制气泡文字
                    drawContext.canvas.nativeCanvas.drawText(
                        tooltipText,
                        boxX,
                        boxY - 15f,
                        paint
                    )
                }
            }
        }
    }
}