package com.yhx.autoledger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
// ✨ 新增：Navigation 的必备导入
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.ui.components.DoubleBackToExitHandler
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.components.ManualAddSheet
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.navigation.Screen.AI.icon
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.CategoryManageScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.screens.HomeScreen
import com.yhx.autoledger.ui.screens.SettingsScreen
import com.yhx.autoledger.ui.screens.DataImportExportScreen // ✨ 导入刚才写好的子页面
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.ui.theme.AutoLedgerTheme
import com.yhx.autoledger.viewmodel.HomeViewModel
import com.yhx.autoledger.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var categoryDao: CategoryDao

    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        lifecycleScope.launch {
            categoryDao.getAllCategories().collect { categories ->
                Log.d("DB_TEST", "数据库被唤醒啦！当前有 ${categories.size} 个分类")
            }
        }

        setContent {
            val themePreference by mainViewModel.themePreference.collectAsState()

            AutoLedgerTheme(themePreference = themePreference) {


                // ✨ 1. 创建全局唯一的导航控制器
                val navController = rememberNavController()

                // ✨ 2. 使用 NavHost 构建路由图
                NavHost(
                    navController = navController,
                    startDestination = "main_tabs" // 起始页面是带底部导航栏的主模块
                ) {

                    // ==========================================
                    // 🚀 模块一：带有 BottomBar 和 Pager 的主模块
                    // ==========================================
                    composable("main_tabs") {
                        DoubleBackToExitHandler()
                        val tabOrder = remember {
                            listOf(Screen.Home, Screen.Detail, Screen.AI, Screen.Settings)
                        }

                        val pagerState = rememberPagerState(pageCount = { tabOrder.size })
                        val coroutineScope = rememberCoroutineScope()
                        var showAddSheet by remember { mutableStateOf(false) }

                        Scaffold(
                            bottomBar = {
                                MainBottomBar(
                                    currentRoute = tabOrder[pagerState.currentPage].route,
                                    onNavigate = { route ->
                                        val targetIndex =
                                            tabOrder.indexOfFirst { it.route == route }
                                        if (targetIndex != -1) {
                                            coroutineScope.launch {
                                                pagerState.scrollToPage(targetIndex)
                                            }
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                if (pagerState.currentPage == 0 || pagerState.currentPage == 1) {
                                    FloatingActionButton(
                                        onClick = { showAddSheet = true },
                                        containerColor = AppDesignSystem.colors.brandAccent,
                                        contentColor = Color.White,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .padding(bottom = 16.dp)
                                            .bounceClick()
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "手动记账",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            },
                            containerColor = AppDesignSystem.colors.appBackground
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize(),
                                    beyondBoundsPageCount = 2
                                ) { pageIndex ->
                                    when (tabOrder[pageIndex].route) {
                                        Screen.Home.route -> HomeScreen()
                                        Screen.Detail.route -> DetailScreen()
                                        Screen.AI.route -> AIScreen()

                                        // ✨ 3. 修复报错核心：传递正确的参数，去除多余的逗号
                                        Screen.Settings.route -> SettingsScreen(
                                            currentTheme = themePreference,
                                            onThemeChange = { newTheme ->
                                                mainViewModel.updateTheme(newTheme)
                                            },
                                            onNavigateToImportExport = {
                                                navController.navigate(Screen.DataImportExport.route)
                                            },
                                            onNavigateToCategoryManage = { // ✨ 在这里实现真正的导航跳转
                                                navController.navigate(Screen.CategoryManage.route)
                                            }
                                        )
                                    }
                                }
                            }

                            // 挂载弹窗组件
                            val homeViewModel: HomeViewModel = hiltViewModel()

                            if (showAddSheet) {
                                ManualAddSheet(
                                    onDismiss = { showAddSheet = false },
                                    // 确保参数列表包含 icon (String类型)
                                    onSave = { type, category, icon, amount, remark, timestamp ->
                                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                                        // homeViewModel.addLedger 要求的 categoryIcon 是 String
                                        homeViewModel.addLedger(
                                            amount = parsedAmount,
                                            type = type,
                                            categoryName = category,
                                            categoryIcon = icon, // ✨ 直接使用传回来的 emoji 或字符串
                                            timestamp = timestamp,
                                            note = remark
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 🚀 模块二：无底部导航的独立子页面 (全屏覆盖)
                    // ==========================================
                    composable(Screen.DataImportExport.route) {
                        DataImportExportScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.CategoryManage.route) {
                        CategoryManageScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    // 未来其他的子页面（如分类管理等）都可以在这里继续添加！
                }
            }
        }
    }
}