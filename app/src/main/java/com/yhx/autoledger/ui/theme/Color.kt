package com.yhx.autoledger.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 基础色
val AccentBlue = Color(0xFF00A8FF)
val GlassWhite = Color(0xCCFFFFFF) // 80%透明白

// 渐变色
val LightBlueGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFE0F2F1), Color(0xFFB2EBF2))
)

// 图标分类色（保持浅色系但有区分）
val CategoryFood = Color(0xFFFF7675)
val CategoryTransport = Color(0xFF74EBD5)
val CategoryShop = Color(0xFFFAB1A0)
val CategoryOther = Color(0xFF81ECEC)