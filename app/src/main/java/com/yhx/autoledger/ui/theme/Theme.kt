package com.yhx.autoledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 定义我们要用的浅色主题配色
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF74EBD5),      // 我们渐变色的主色调
    secondary = Color(0xFF9FACE6),    // 辅助色
    background = Color(0xFFF7F9FC),   // 之前定义的浅蓝色背景
    surface = Color.White             // 卡片等表面的颜色
)

@Composable
fun AutoLedgerTheme(
    content: @Composable () -> Unit
) {
    // 暂时只做浅色模式，保持 UI 高级感统一
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography, // 这里引用的是 Type.kt 里的定义
        content = content
    )
}