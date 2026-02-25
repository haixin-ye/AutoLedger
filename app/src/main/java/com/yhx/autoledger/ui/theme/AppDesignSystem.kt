package com.yhx.autoledger.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==============================================================================
//  🎨 App 全局尺寸规范 (Foundation Dimensions)
// ==============================================================================
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

// ==============================================================================
//  🎨 App 全局颜色字典 (Foundation & Component Colors)
// ==============================================================================
data class AppExtendedColors(
    // ----------------------------------------------------
    // 1. 全局基础色 (Foundation Backgrounds)
    // ----------------------------------------------------
    val appBackground: Color,       // App大背景 (浅色蓝灰/深色纯黑)
    val cardBackground: Color,      // 通用卡片底色 (如各种列表项的白底)
    val bottomBarBackground: Color, // 底部导航栏底色
    val sheetBackground: Color,     // 底部弹窗(BottomSheet)背景
    val surfaceVariant: Color,      // 表层浅灰底块(输入框、未选中按钮、进度条底槽等)

    // ----------------------------------------------------
    // 2. 全局文本色 (Foundation Typography)
    // ----------------------------------------------------
    val textPrimary: Color,         // 主标题、核心大字 (最强对比度)
    val textSecondary: Color,       // 副标题、常规正文 (中等对比度)
    val textTertiary: Color,        // 极弱提示语、水印 (最低对比度)
    val textOnAccent: Color,        // 在品牌色/强调色块上显示的文字 (通常是白色)

    // ----------------------------------------------------
    // 3. 核心财务语义色 (Financial Semantics)
    // ----------------------------------------------------
    val incomeColor: Color,         // 收入 (绿色系)
    val expenseColor: Color,        // 支出 (红色系)
    val brandAccent: Color,         // App 主品牌强调色 (AccentBlue)

    // ----------------------------------------------------
    // 4. 辅助状态色与基础分类色 (States & Categories)
    // ----------------------------------------------------
    val dividerColor: Color,        // 通用分割线颜色
    val warningRed: Color,          // 警示/报错红色
    val categoryFood: Color,        // 餐饮分类色
    val categoryTransport: Color,   // 交通分类色
    val categoryShop: Color,        // 购物分类色
    val categoryOther: Color,       // 其他分类色

    // ====================================================
    // 以下为组件专属定制色 (Component-Specific Colors)
    // ====================================================

    // ----------------------------------------------------
    // [首页] 主资产卡片 (MainBalanceCard)
    // ----------------------------------------------------
    val balanceCardGradientStart: Color,    // 卡片渐变起点
    val balanceCardGradientEnd: Color,      // 卡片渐变终点
    val balanceCardTextPrimary: Color,      // 卡片上的主文本 (强对比)
    val balanceCardTextSecondary: Color,    // 卡片上的副文本 (弱对比)
    val balanceCardDailyAvgBg: Color,       // 日均可用的小底块背景
    val balanceCardDailyAvgText: Color,     // 日均可用的小底块文字
    val balanceCardCircleDecoration: Color, // 右上角装饰圆的颜色

    // ----------------------------------------------------
    // [首页] 预算健康度仪表盘 (DoubleCircleGauges)
    // ----------------------------------------------------
    val gaugeSafe: Color,          // 预算充足 (绿)
    val gaugeWarning: Color,       // 预算预警 (黄)
    val gaugeDanger: Color,        // 预算超支 (红)
    val gaugeTrack: Color,         // 未走过进度的底轨色

    // ----------------------------------------------------
    // [首页] 批量删除悬浮条 (BatchActionBar)
    // ----------------------------------------------------
    val batchActionBarBg: Color,        // 悬浮条背景色 (苹果高级灰)
    val batchActionBarContent: Color,   // 未选中内容颜色
    val batchActionBarActive: Color,    // 选中/高亮状态颜色
    val batchActionBarDivider: Color,   // 内部柔和分割线

    // ----------------------------------------------------
    // [通用] 记账输入弹窗 (TransactionSheet)
    // ----------------------------------------------------
    val sheetTabBackground: Color,        // 顶部支出/收入切换器的底槽颜色
    val sheetTabSelectedBg: Color,        // 选中 Tab 的背景块颜色
    val sheetTabSelectedText: Color,      // 选中 Tab 的文字颜色
    val sheetTabUnselectedText: Color,    // 未选中 Tab 的文字颜色
    val sheetInputBackground: Color,      // 金额、备注输入框的底色
    val sheetCategoryBgUnselected: Color, // 未选中分类的图标底色

    // ----------------------------------------------------
    // [明细] 顶部账单日历 (CalendarDetailView)
    // ----------------------------------------------------
    val calendarTodayText: Color,       // 今日日期的数字颜色
    val calendarTodayBackground: Color, // 今日日期的底块颜色

    // ----------------------------------------------------
    // [明细] 数据总览面板 (PremiumBlockCard)
    // ----------------------------------------------------
    val overviewIndicatorColor: Color,  // 标题竖线及进度百分比文字
    val overviewSubPanelBg: Color,      // 底部次要数据面板底色

    // ----------------------------------------------------
    // [明细] 日消费趋势图表 (DailyTrendChart)
    // ----------------------------------------------------
    val chartToggleBg: Color,             // 右上角样式切换器的底色
    val chartToggleSelectedBg: Color,     // 切换器选中项背景
    val chartToggleSelectedText: Color,   // 切换器选中项文字
    val chartToggleUnselectedText: Color, // 切换器未选中项文字
    val chartGridLine: Color,             // 背景网格虚线
    val chartAxisText: Color,             // 坐标轴刻度文字
    val chartGradientStart: Color,        // 图表折线/柱状的主体渐变起点
    val chartGradientEnd: Color,          // 图表折线/柱状的主体渐变终点
    val chartTooltipLine: Color,          // 长按浮窗跟随的垂直线/内圆点
    val chartTooltipCircleOuter: Color,   // 长按浮窗跟随的外圆点
    val chartTooltipBubbleBg: Color,      // 浮窗高级磨砂气泡背景
    val chartTooltipBubbleText: Color,    // 浮窗文字颜色

    // ----------------------------------------------------
    // [明细] 高级甜甜圈图表 (PremiumDonutChart)
    // ----------------------------------------------------
    val donutChartTrack: Color,          // 甜甜圈底层灰色轨道
    val donutChartTextPrimary: Color,    // 中心金额主要文字
    val donutChartTextSecondary: Color,  // 中心副标题
    val donutChartPalette: List<Pair<Color, Color>>, // 动态渐变色板 (10组)

    // ----------------------------------------------------
    // [AI助手] 聊天气泡模块 (ChatComponents)
    // ----------------------------------------------------
    val chatUserBubble: Color,      // 用户发送的气泡底色
    val chatAiBubble: Color,        // AI 回复的气泡底色
    val chatAiText: Color,          // AI 回复气泡上的文字色
    val chatUserHeadCircle: Color,  // 用户头像的底色光晕
    val chatAIHeadCircle: Color,    // AI 头像的底色光晕

    // ----------------------------------------------------
    // [我的] 设置页图标色块与专属组件 (SettingsScreen)
    // ----------------------------------------------------
    val iconBgAI: Color,            // AI设置图标底色
    val iconBgSecurity: Color,      // 安全锁图标底色
    val iconBgCloud: Color,         // 云端同步图标底色
    val iconBgExport: Color,        // 数据导出图标底色
    val iconBgTheme: Color,         // 主题切换图标底色
    val iconBgAlert: Color,         // 记账提醒图标底色

    val profileGradientStart: Color,        // 个人卡片渐变起点
    val profileGradientEnd: Color,          // 个人卡片渐变终点
    val profileTextOnGradient: Color,       // 个人卡片主文字
    val profileTextSubOnGradient: Color,    // 个人卡片副文字
    val profileAvatarBgOnGradient: Color,   // 头像底色
    val profileAvatarIconOnGradient: Color, // 头像图标色

    val switchThumbChecked: Color,  // Switch 开启时的圆形滑块颜色
    val logoutButtonText: Color,    // 退出登录按钮警示字
    val dialogConfirmText: Color    // 弹窗确认按钮文字色
)

// ==============================================================================
//  ☀️ 浅色主题实例 (Light Theme Implementation)
// ==============================================================================
val LightAppColors = AppExtendedColors(
    // --- 1. 全局基础色 ---
    appBackground = Color(0xFFF7F9FC),
    cardBackground = Color.White,
    bottomBarBackground = Color.White,
    sheetBackground = Color.White,
    surfaceVariant = Color(0xFFF1F2F6),

    // --- 2. 全局文本色 ---
    textPrimary = Color.Black,
    textSecondary = Color.Gray,
    textTertiary = Color.LightGray,
    textOnAccent = Color.White,

    // --- 3. 核心财务语义色 ---
    incomeColor = Color(0xFF00C853),
    expenseColor = Color(0xFFFF5252),
    brandAccent = Color(0xFF00A8FF), // 你的 AccentBlue

    // --- 4. 辅助状态与分类 ---
    dividerColor = Color(0xFFF1F2F6),
    warningRed = Color.Red,
    categoryFood = Color(0xFFFF7675),
    categoryTransport = Color(0xFF74EBD5),
    categoryShop = Color(0xFFFAB1A0),
    categoryOther = Color(0xFF81ECEC),

    // --- [首页] 主资产卡片 (✨ 修复：还原高透亮蓝色渐变) ---
    balanceCardGradientStart = Color(0xFF4776E6), // 柔和群青
    balanceCardGradientEnd = Color(0xFF8E54E9),   // 优雅深紫
    balanceCardTextPrimary = Color.White,
    balanceCardTextSecondary = Color.White.copy(alpha = 0.8f),
    balanceCardDailyAvgBg = Color.White.copy(alpha = 0.2f),
    balanceCardDailyAvgText = Color.White,
    balanceCardCircleDecoration = Color.White.copy(alpha = 0.1f),

    // --- [首页] 预算健康度仪表盘 ---
    gaugeSafe = Color(0xFF2ED573),
    gaugeWarning = Color(0xFFFFC107),
    gaugeDanger = Color(0xFFFF4757),
    gaugeTrack = Color.LightGray.copy(alpha = 0.3f),

    // --- [首页] 批量删除悬浮条 ---
    batchActionBarBg = Color(0xFF2C2C2E),
    batchActionBarContent = Color.White,
    batchActionBarActive = Color(0xFF8FD3F4),
    batchActionBarDivider = Color(0xFF48484A),

    // --- [通用] 记账输入弹窗 ---
    sheetTabBackground = Color.LightGray.copy(alpha = 0.2f),
    sheetTabSelectedBg = Color.White,
    sheetTabSelectedText = Color.Black,
    sheetTabUnselectedText = Color.Gray,
    sheetInputBackground = Color.White,
    sheetCategoryBgUnselected = Color.White,

    // --- [明细] 顶部账单日历 ---
    calendarTodayText = Color(0xFF1976D2),
    calendarTodayBackground = Color(0xFFDADDE0),

    // --- [明细] 数据总览面板 ---
    overviewIndicatorColor = Color(0xFF8FD3F4),
    overviewSubPanelBg = Color(0xFFF8F9FB),

    // --- [明细] 日消费趋势图表 ---
    chartToggleBg = Color(0xFFF1F3F6),
    chartToggleSelectedBg = Color.White,
    chartToggleSelectedText = Color.Black,
    chartToggleUnselectedText = Color.Gray,
    chartGridLine = Color.LightGray.copy(alpha = 0.3f),
    chartAxisText = Color.LightGray,
    chartGradientStart = Color(0xFF8FD3F4),
    chartGradientEnd = Color(0xFF84FAB0),
    chartTooltipLine = Color(0xFF8FD3F4),
    chartTooltipCircleOuter = Color.White,
    chartTooltipBubbleBg = Color(0xFF2D3436),
    chartTooltipBubbleText = Color.White,

    // --- [明细] 高级甜甜圈图表 ---
    donutChartTrack = Color(0xFFF1F3F6),
    donutChartTextPrimary = Color(0xFF1F2937),
    donutChartTextSecondary = Color(0xFF9CA3AF),
    donutChartPalette = listOf(
        Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4)), // 1. 薄荷绿 -> 晴空蓝 (清新)
        Pair(Color(0xFFF6D365), Color(0xFFFDA085)), // 2. 柠檬黄 -> 蜜桃橙 (温暖)
        Pair(Color(0xFFFF9A9E), Color(0xFFFECFEF)), // 3. 樱花粉 -> 珍珠白 (柔美)
        Pair(Color(0xFF89F7FE), Color(0xFF66A6FF)), // 4. 纯净冰蓝 (冷峻)
        Pair(Color(0xFFE2B0FF), Color(0xFF9F44D3)), // 5. 唯一香芋紫 (高贵)
        Pair(Color(0xFFD4FC79), Color(0xFF96E6A1)), // 6. 抹茶黄绿 (生机)
        Pair(Color(0xFFff9a44), Color(0xFFfc6076)), // 7. 珊瑚橘红 (热情)
        Pair(Color(0xFFA6C0FE), Color(0xFFF68084)), // 8. 灰蓝 -> 绯红 (高级撞色)
        Pair(Color(0xFFFDEB71), Color(0xFFF8D800)), // 9. 亮金明黄 (明亮)
        Pair(Color(0xFF43E97B), Color(0xFF38F9D7))  // 10. 极光青色 (科技)
    ),

    // --- [AI助手] 聊天气泡模块 ---
    chatUserBubble = Color(0xFF00A8FF), // AccentBlue
    chatAiBubble = Color.White,
    chatAiText = Color(0xFF1D1D1F),
    chatUserHeadCircle = Color(0xFFE3F2FD),
    chatAIHeadCircle = Color(0xFFFFF3E0),

    // --- [我的] 设置页与图标 ---
    iconBgAI = Color(0xFF74EBD5),
    iconBgSecurity = Color(0xFFFF7675),
    iconBgCloud = Color(0xFF9FACE6),
    iconBgExport = Color(0xFFFAB1A0),
    iconBgTheme = Color(0xFF81ECEC),
    iconBgAlert = Color(0xFFFFB8B8),

    profileGradientStart = Color(0xFFE3F2FD),
    profileGradientEnd = Color(0xFFBBDEFB),
    profileTextOnGradient = Color.Black,
    profileTextSubOnGradient = Color(0xFF00A8FF),
    profileAvatarBgOnGradient = Color.White.copy(alpha = 0.3f),
    profileAvatarIconOnGradient = Color.White,
    switchThumbChecked = Color.White,
    logoutButtonText = Color.Red.copy(alpha = 0.8f),
    dialogConfirmText = Color(0xFF00A8FF)
)


// ==============================================================================
//  🌙 深色主题实例 (Dark Theme - 哑光黑金极致沉稳版 Matte Black & Gold)
// ==============================================================================
val DarkAppColors = AppExtendedColors(
    // --- 1. 全局基础色 (带有暖色调的炭黑，彻底拒绝纯黑，增加磨砂质感) ---
    appBackground = Color(0xFF161618),      // 暖炭灰黑
    cardBackground = Color(0xFF222225),     // 微微下沉的磨砂深灰
    bottomBarBackground = Color(0xFF1C1C1E),
    sheetBackground = Color(0xFF1E1E20),
    surfaceVariant = Color(0xFF2A2A2D),     // 柔和的表层灰槽

    // --- 2. 全局文本色 (温润的羊皮纸白，彻底拒绝刺眼的纯白) ---
    textPrimary = Color(0xFFE8E6E1),        // 暖灰白 (像古董纸张，对比度极其舒适)
    textSecondary = Color(0xFFA3A19C),      // 亚麻灰
    textTertiary = Color(0xFF6B6A67),       // 深灰
    textOnAccent = Color(0xFF161618),       // 金色底块上的字，用深灰色最显高级

    // --- 3. 核心财务语义色 (✨ 核心：低饱和哑光复古色) ---
    incomeColor = Color(0xFF6B8E6B),        // 哑光鼠尾草绿 (沉稳不刺眼)
    expenseColor = Color(0xFFB86B6B),       // 哑光砖红/豆沙红 (内敛的高级红)
    brandAccent = Color(0xFFD4B26A),        // 哑光香槟金 (不土气的高级金)

    // --- 4. 辅助状态与分类 ---
    dividerColor = Color(0xFF2A2A2D),
    warningRed = Color(0xFFB86B6B),         // 统一使用哑光砖红
    categoryFood = Color(0xFFB86B6B),
    categoryTransport = Color(0xFF7A9C7A),
    categoryShop = Color(0xFFD4B26A),
    categoryOther = Color(0xFF7A8B9C),      // 哑光灰蓝

    // --- [首页] 主资产卡片 (低调的黑金哑光卡片) ---
    balanceCardGradientStart = Color(0xFF2E2D2A), // 微弱的暖金灰
    balanceCardGradientEnd = Color(0xFF1A1A1C),   // 沉稳的黑
    balanceCardTextPrimary = Color(0xFFD4B26A),   // 主金额使用香槟金
    balanceCardTextSecondary = Color(0xFFA3A19C),
    balanceCardDailyAvgBg = Color(0xFFD4B26A).copy(alpha = 0.08f), // 极微弱的金色底
    balanceCardDailyAvgText = Color(0xFFD4B26A),
    balanceCardCircleDecoration = Color(0xFFD4B26A).copy(alpha = 0.03f),

    // --- [首页] 预算健康度仪表盘 (配套的哑光色) ---
    gaugeSafe = Color(0xFF6B8E6B),          // 哑光绿
    gaugeWarning = Color(0xFFD4B26A),       // 哑光金
    gaugeDanger = Color(0xFFB86B6B),        // 哑光红
    gaugeTrack = Color(0xFF333336),         // 磨砂底轨

    // --- [首页] 批量删除悬浮条 ---
    batchActionBarBg = Color(0xFF2A2A2D),
    batchActionBarContent = Color(0xFFE8E6E1),
    batchActionBarActive = Color(0xFFD4B26A),
    batchActionBarDivider = Color(0xFF4A4A4D),

    // --- [通用] 记账输入弹窗 ---
    sheetTabBackground = Color(0xFF2A2A2D),
    sheetTabSelectedBg = Color(0xFF3A3A3D),
    sheetTabSelectedText = Color(0xFFE8E6E1),
    sheetTabUnselectedText = Color(0xFFA3A19C),
    sheetInputBackground = Color(0xFF262629),
    sheetCategoryBgUnselected = Color(0xFF262629),

    // --- [明细] 顶部账单日历  ---
    calendarTodayText = Color(0xFFD4B26A),       // 日期数字变成香槟金
    calendarTodayBackground = Color(0xFF2A2A2D), // 底块变成磨砂深灰

    // --- [明细] 数据总览面板 ---
    overviewIndicatorColor = Color(0xFFD4B26A),
    overviewSubPanelBg = Color(0xFF262629),

    // --- [明细] 日消费趋势图表 ---
    chartToggleBg = Color(0xFF2A2A2D),
    chartToggleSelectedBg = Color(0xFF3A3A3D),
    chartToggleSelectedText = Color(0xFFE8E6E1),
    chartToggleUnselectedText = Color(0xFFA3A19C),
    chartGridLine = Color(0xFF333336),
    chartAxisText = Color(0xFFA3A19C),
    chartGradientStart = Color(0xFFD4B26A),     // 图表金
    chartGradientEnd = Color(0xFF9E844D),       // 沉稳暗金
    chartTooltipLine = Color(0xFFD4B26A),
    chartTooltipCircleOuter = Color(0xFF222225),
    chartTooltipBubbleBg = Color(0xFFD4B26A),
    chartTooltipBubbleText = Color(0xFF161618),

    // --- [明细] 高级甜甜圈图表 (✨ 专属调配：莫兰迪/哑光金属质感色板) ---
    donutChartTrack = Color(0xFF2A2A2D),
    donutChartTextPrimary = Color(0xFFE8E6E1),
    donutChartTextSecondary = Color(0xFFA3A19C),
    donutChartPalette = listOf(
        Pair(Color(0xFFD4B26A), Color(0xFF9E844D)), // 1. 哑光香槟金
        Pair(Color(0xFFB87A7A), Color(0xFF8A5C5C)), // 2. 复古干玫瑰
        Pair(Color(0xFF7A9C7A), Color(0xFF5A755A)), // 3. 莫兰迪灰绿
        Pair(Color(0xFF7A8B9C), Color(0xFF5C6A7A)), // 4. 沉稳灰蓝
        Pair(Color(0xFFB88A5C), Color(0xFF8A6745)), // 5. 古董黄铜
        Pair(Color(0xFF9C7A9C), Color(0xFF755C75)), // 6. 哑光灰紫
        Pair(Color(0xFFC27A5C), Color(0xFF915C45)), // 7. 陶土橘棕
        Pair(Color(0xFF5C9C8A), Color(0xFF457567)), // 8. 氧化灰青
        Pair(Color(0xFFA3A39C), Color(0xFF7A7A75)), // 9. 铅锡灰银
        Pair(Color(0xFF8A4A4A), Color(0xFF663636))  // 10. 深暗酒红
    ),

    // --- [AI助手] 聊天气泡模块 ---
    chatUserBubble = Color(0xFFD4B26A),
    chatAiBubble = Color(0xFF2A2A2D),
    chatAiText = Color(0xFFE8E6E1),
    chatUserHeadCircle = Color(0xFFD4B26A).copy(alpha = 0.15f),
    chatAIHeadCircle = Color(0xFF2A2A2D),

    // --- [我的] 设置页与图标 (全部采用莫兰迪深色) ---
    iconBgAI = Color(0xFFB8944A),
    iconBgSecurity = Color(0xFF9E6B6B),
    iconBgCloud = Color(0xFF6B7A8A),
    iconBgExport = Color(0xFF947A5C),
    iconBgTheme = Color(0xFF6B8A7A),
    iconBgAlert = Color(0xFF8A5C5C),

    profileGradientStart = Color(0xFF2E2D2A),
    profileGradientEnd = Color(0xFF1A1A1C),
    profileTextOnGradient = Color(0xFFD4B26A),
    profileTextSubOnGradient = Color(0xFFA3A19C),
    profileAvatarBgOnGradient = Color(0xFFD4B26A).copy(alpha = 0.1f),
    profileAvatarIconOnGradient = Color(0xFFD4B26A),

    switchThumbChecked = Color(0xFF161618),
    logoutButtonText = Color(0xFFB86B6B),       // 哑光红
    dialogConfirmText = Color(0xFFD4B26A)       // 哑光金
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalAppDimens = staticCompositionLocalOf { AppDimens() }