package com.yhx.autoledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Detail : Screen("detail", "明细", Icons.Default.List)
    object AI : Screen("ai", "AI助手", Icons.Default.Face) // 或者是麦克风图标
    object Settings : Screen("settings", "我的", Icons.Default.Settings)

    object DataImportExport : Screen("data_import_export", "导入导出", Icons.Rounded.ImportExport)

    object CategoryManage : Screen("category_manage", "分类管理", Icons.Rounded.Category)

    // ✨ 新增账本管理路由
    object BookManage : Screen("book_manage", "账本管理", Icons.Rounded.MenuBook)
//    // 传递 categoryName 和主题色 index
//    object CategoryDetail : Screen("category_detail/{categoryName}/{colorIndex}", "分类详情", Icons.Default.List) {
//        fun createRoute(categoryName: String, colorIndex: Int) = "category_detail/$categoryName/$colorIndex"
//    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Detail,
    Screen.AI,
    Screen.Settings
)