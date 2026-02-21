package com.yhx.autoledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Detail : Screen("detail", "明细", Icons.Default.List)
    object AI : Screen("ai", "AI助手", Icons.Default.Face) // 或者是麦克风图标
    object Settings : Screen("settings", "我的", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Detail,
    Screen.AI,
    Screen.Settings
)