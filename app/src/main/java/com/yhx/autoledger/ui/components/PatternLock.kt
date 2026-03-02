package com.yhx.autoledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.yhx.autoledger.ui.theme.AppDesignSystem
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun PatternLock(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onPatternComplete: (List<Int>) -> Unit
) {
    var selectedDots by remember { mutableStateOf(emptyList<Int>()) }
    var currentPos by remember { mutableStateOf<Offset?>(null) }

    // 颜色配置
    val normalDotColor = AppDesignSystem.colors.textTertiary
    val activeDotColor = if (isError) AppDesignSystem.colors.warningRed else AppDesignSystem.colors.brandAccent
    val lineColor = activeDotColor.copy(alpha = 0.6f)

    // 清空状态的触发器
    LaunchedEffect(isError) {
        if (isError) {
            // 如果外部传进来是错误状态，延迟 1 秒后清空连线，让用户重画
            kotlinx.coroutines.delay(300)
            selectedDots = emptyList()
            currentPos = null
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(isError) {
                if (isError) return@pointerInput // 错误时短暂冻结输入
                detectDragGestures(
                    onDragStart = { pos ->
                        selectedDots = emptyList()
                    },
                    onDragEnd = {
                        currentPos = null
                        if (selectedDots.isNotEmpty()) {
                            onPatternComplete(selectedDots)
                        }
                    },
                    onDragCancel = {
                        currentPos = null
                        selectedDots = emptyList()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val pos = change.position
                        currentPos = pos

                        // 计算 3x3 网格的宽高
                        val cellWidth = size.width / 3
                        val cellHeight = size.height / 3
                        val hitRadius = cellWidth / 3f // 触摸生效半径

                        // 检测是否划过了某个点
                        for (i in 0 until 9) {
                            val col = i % 3
                            val row = i / 3
                            val dotX = col * cellWidth + cellWidth / 2
                            val dotY = row * cellHeight + cellHeight / 2

                            val distance = sqrt((pos.x - dotX).pow(2) + (pos.y - dotY).pow(2))
                            if (distance < hitRadius && !selectedDots.contains(i)) {
                                selectedDots = selectedDots + i
                            }
                        }
                    }
                )
            }
    ) {
        val cellWidth = size.width / 3
        val cellHeight = size.height / 3

        // 1. 画已连接的线
        if (selectedDots.isNotEmpty()) {
            val path = Path()
            selectedDots.forEachIndexed { index, dot ->
                val col = dot % 3
                val row = dot / 3
                val dotX = col * cellWidth + cellWidth / 2
                val dotY = row * cellHeight + cellHeight / 2

                if (index == 0) {
                    path.moveTo(dotX, dotY)
                } else {
                    path.lineTo(dotX, dotY)
                }
            }
            // 连向当前手指位置
            if (currentPos != null) {
                path.lineTo(currentPos!!.x, currentPos!!.y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 2. 画 9 个点
        for (i in 0 until 9) {
            val col = i % 3
            val row = i / 3
            val dotX = col * cellWidth + cellWidth / 2
            val dotY = row * cellHeight + cellHeight / 2

            val isSelected = selectedDots.contains(i)
            drawCircle(
                color = if (isSelected) activeDotColor else normalDotColor.copy(alpha = 0.3f),
                radius = if (isSelected) 14.dp.toPx() else 8.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            // 选中点外圈的浅色光晕
            if (isSelected) {
                drawCircle(
                    color = activeDotColor.copy(alpha = 0.2f),
                    radius = 32.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}