package com.yhx.autoledger.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val spacingTiny: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingNormal: Dp = 16.dp,
    val spacingMedium: Dp = 20.dp,
    val spacingLarge: Dp = 24.dp,
    val cardElevation: Dp = 2.dp,
    val cardCornerRadius: Dp = 24.dp,
    val buttonHeight: Dp = 50.dp,
    val listItemHeight: Dp = 60.dp,
    val iconSizeNormal: Dp = 20.dp,
    val iconBgSize: Dp = 36.dp
)

data class AppExtendedColors(

    // --- 日历组件专属 ---
    val calendarTodayText: Color,          // 今日日期的数字颜色
    val calendarTodayBackground: Color,    // 今日日期的底块颜色

    // --- 我的页面：个人信息卡片专属状态 ---
    val profileGradientStart: Color,           // 登录后卡片渐变起点
    val profileGradientEnd: Color,             // 登录后卡片渐变终点
    val profileTextOnGradient: Color,          // 登录后卡片上的主文字 (原 Black)
    val profileTextSubOnGradient: Color,       // 登录后卡片上的副文字 (原 AccentBlue)
    val profileAvatarBgOnGradient: Color,      // 登录后头像底色 (原 White 30%)
    val profileAvatarIconOnGradient: Color,    // 登录后头像图标色 (原 White)

    // --- 通用/设置页控件专属 ---
    val switchThumbChecked: Color,             // Switch 开启时的圆形滑块颜色
    val logoutButtonText: Color,               // 退出登录按钮的警示红字
    val dialogConfirmText: Color,              // 弹窗确认按钮文字色

    // 基础背景
    val appBackground: Color,       // App大背景
    val cardBackground: Color,      // 卡片底色
    val bottomBarBackground: Color, // 底部导航栏
    val sheetBackground: Color,     // 底部弹窗(BottomSheet)背景
    val surfaceVariant: Color,      // 各种浅灰色底块(输入框、未选中按钮、进度条底槽)

    // 文本颜色
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,        // 品牌色上的文字(通常是白色)

    // 财务与品牌色
    val incomeColor: Color,
    val expenseColor: Color,
    val brandAccent: Color,         // 替代 AccentBlue

    // 分类颜色 (环形图等使用)
    val categoryFood: Color,
    val categoryTransport: Color,
    val categoryShop: Color,
    val categoryOther: Color,

    // AI 聊天模块专用
    val chatUserBubble: Color,
    val chatAiBubble: Color,
    val chatAiText: Color,
    val chatUserHeadCircle: Color,
    val chatAIHeadCircle: Color,

    // 设置页图标底色
    val iconBgAI: Color,
    val iconBgSecurity: Color,
    val iconBgCloud: Color,
    val iconBgExport: Color,
    val iconBgTheme: Color,
    val iconBgAlert: Color,

    // 辅助颜色
    val dividerColor: Color,
    val warningRed: Color
)

val LightAppColors = AppExtendedColors(
    appBackground = Color(0xFFF7F9FC),
    cardBackground = Color.White,
    bottomBarBackground = Color.White,
    sheetBackground = Color.White,
    surfaceVariant = Color(0xFFF1F2F6), // 替代原先所有的 0xFFF1F2F6

    textPrimary = Color.Black,
    textSecondary = Color.Gray,
    textTertiary = Color.LightGray,
    textOnAccent = Color.White,

    incomeColor = Color(0xFF00C853),
    expenseColor = Color(0xFFFF5252),
    brandAccent = AccentBlue,

    categoryFood = Color(0xFFFF7675),
    categoryTransport = Color(0xFF74EBD5),
    categoryShop = Color(0xFFFAB1A0),
    categoryOther = Color(0xFF81ECEC),


    //AI
    chatUserBubble = AccentBlue,
    chatAiBubble = Color.White,
    chatAiText = Color(0xFF1D1D1F),
    chatUserHeadCircle = Color(0xFFE3F2FD),
    chatAIHeadCircle = Color(0xFFFFF3E0),

    //ICON
    iconBgAI = Color(0xFF74EBD5),
    iconBgSecurity = Color(0xFFFF7675),
    iconBgCloud = Color(0xFF9FACE6),
    iconBgExport = Color(0xFFFAB1A0),
    iconBgTheme = Color(0xFF81ECEC),
    iconBgAlert = Color(0xFFFFB8B8),

    dividerColor = Color(0xFFF1F2F6),
    warningRed = Color.Red,


    //Detail
    calendarTodayText = Color(0xFF1976D2),
    calendarTodayBackground = Color(0xFFDADDE0),

    //Setting
    profileGradientStart = Color(0xFFE3F2FD), // 💡 请替换为原 LightBlueGradient 的实际起点色
    profileGradientEnd = Color(0xFFBBDEFB),   // 💡 请替换为原 LightBlueGradient 的实际终点色
    profileTextOnGradient = Color.Black,
    profileTextSubOnGradient = Color(0xFF00A8FF), // 原 AccentBlue
    profileAvatarBgOnGradient = Color.White.copy(alpha = 0.3f),
    profileAvatarIconOnGradient = Color.White,

    switchThumbChecked = Color.White,
    logoutButtonText = Color.Red.copy(alpha = 0.8f),
    dialogConfirmText = Color(0xFF00A8FF)
)

val DarkAppColors = AppExtendedColors(
    appBackground = Color(0xFF121212),
    cardBackground = Color(0xFF1E1E1E),
    bottomBarBackground = Color(0xFF1E1E1E),
    sheetBackground = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),

    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFFA0A0A0),
    textTertiary = Color(0xFF666666),
    textOnAccent = Color.White,

    incomeColor = Color(0xFF69F0AE),
    expenseColor = Color(0xFFFF8A80),
    brandAccent = Color(0xFF40C4FF),

    categoryFood = Color(0xFFD63031),
    categoryTransport = Color(0xFF00CEC9),
    categoryShop = Color(0xFFE17055),
    categoryOther = Color(0xFF00B894),

    chatUserBubble = Color(0xFF007ACC),
    chatAiBubble = Color(0xFF2C2C2C),
    chatAiText = Color(0xFFE0E0E0),
    chatUserHeadCircle = Color(0xFFE3F2FD),
    chatAIHeadCircle = Color(0xFFFFF3E0),

    // 暗色模式下的柔和背景
    iconBgAI = Color(0xFF45B09E),
    iconBgSecurity = Color(0xFFC0392B),
    iconBgCloud = Color(0xFF5A6DAF),
    iconBgExport = Color(0xFFD35400),
    iconBgTheme = Color(0xFF008B8B),
    iconBgAlert = Color(0xFFC0392B),

    dividerColor = Color(0xFF2C2C2C),
    warningRed = Color(0xFFFF5252),


    //Detail
    calendarTodayText = Color(0xFF82B1FF),       // 提亮的亮蓝色，在暗色下更清晰
    calendarTodayBackground = Color(0xFF37474F), // 深蓝灰色作为底块，既明显又不刺眼
    //setting
    profileGradientStart = Color(0xFF1E3C72), // 深色模式下替换为深邃蓝渐变
    profileGradientEnd = Color(0xFF2A5298),
    profileTextOnGradient = Color.White,      // 深色渐变上必须用白字才能看清
    profileTextSubOnGradient = Color(0xFF81D4FA),
    profileAvatarBgOnGradient = Color.Black.copy(alpha = 0.2f),
    profileAvatarIconOnGradient = Color.White,

    switchThumbChecked = Color(0xFFE0E0E0),
    logoutButtonText = Color(0xFFFF5252).copy(alpha = 0.8f),
    dialogConfirmText = Color(0xFF40C4FF)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalAppDimens = staticCompositionLocalOf { AppDimens() }