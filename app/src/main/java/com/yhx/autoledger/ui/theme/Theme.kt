package com.yhx.autoledger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// M3 基础浅色
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00A8FF), // AccentBlue
    secondary = Color(0xFF9FACE6),
    background = Color(0xFFF7F9FC),
    surface = Color.White
)

// M3 基础深色
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00A8FF),
    secondary = Color(0xFF7C8BC2),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

// 提供便捷的单例访问入口，以后代码里就用 AppTheme.colors 和 AppTheme.dimens
object AppDesignSystem {
    val colors: AppExtendedColors
        @Composable
        get() = LocalAppColors.current

    val dimens: AppDimens
        @Composable
        get() = LocalAppDimens.current
}

@Composable
fun AutoLedgerTheme(
    themePreference: Int = 0, // 传入用户的偏好设置
    content: @Composable () -> Unit
) {
    // 判断是否应该使用深色模式
    val isDarkTheme = when (themePreference) {
        1 -> false // 强制浅色
        2 -> true  // 强制深色
        else -> isSystemInDarkTheme() // 0: 跟随系统
    }

    // 根据模式选择对应的颜色字典
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (isDarkTheme) DarkAppColors else LightAppColors

    // 沉浸式状态栏适配
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    // 核心注入：将我们的自定义颜色和尺寸下发到整个 Compose 树
    CompositionLocalProvider(
        LocalAppColors provides extendedColors,
        LocalAppDimens provides AppDimens()
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
            typography = Typography,
            content = content
        )
    }
}