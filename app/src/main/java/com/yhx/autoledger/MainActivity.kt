package com.yhx.autoledger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // ✨ 新增：用于获取 ViewModel
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
import androidx.compose.runtime.collectAsState // ✨ 新增：用于观察 Flow 状态
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
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.ui.components.DoubleBackToExitHandler
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.components.ManualAddSheet
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.screens.HomeScreen
import com.yhx.autoledger.ui.screens.SettingsScreen
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 新增：引入全局主题
import com.yhx.autoledger.ui.theme.AutoLedgerTheme
import com.yhx.autoledger.viewmodel.HomeViewModel
import com.yhx.autoledger.viewmodel.MainViewModel // ✨ 新增：引入 MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var categoryDao: CategoryDao

    // ✨ 新增：注入全局控制的 ViewModel
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 抽掉系统默认的半透明黑色背景，设为完全透明！
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        // ✨ 极其关键的修改：
        // 以前这里硬编码了 isAppearanceLightStatusBars = true，会导致深色模式下状态栏文字依然是黑色的（看不见）。
        // 我们现在的 AutoLedgerTheme 内部已经根据是否是深色模式动态处理了这个逻辑，所以这里不需要写死了。
        // val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // insetsController.isAppearanceLightStatusBars = true
        // insetsController.isAppearanceLightNavigationBars = true


        // 临时测试代码：强行读取一次数据库，触发 onCreate 回调和预设数据注入
        lifecycleScope.launch {
            categoryDao.getAllCategories().collect { categories ->
                Log.d("DB_TEST", "数据库被唤醒啦！当前有 ${categories.size} 个分类")
            }
        }

        setContent {
            // ✨ 1. 观察 DataStore 中的主题偏好 (0:系统 1:浅色 2:深色)
            val themePreference by mainViewModel.themePreference.collectAsState()

            // ✨ 2. 将偏好传给 AutoLedgerTheme
            AutoLedgerTheme(themePreference = themePreference) {
                // 滑动两次退出。
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
                                val targetIndex = tabOrder.indexOfFirst { it.route == route }
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
                                // ✨ 3. 替换硬编码 AccentBlue -> 映射为品牌色
                                containerColor = AppTheme.colors.brandAccent,
                                contentColor = Color.White, // 品牌色上的白色图标保持不变
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
                    // ✨ 4. 替换硬编码 Color(0xFFF7F9FC) -> 映射为全局背景色
                    containerColor = AppTheme.colors.appBackground
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

                                // ✨ 5. 修复此处报错：传入对应的参数
                                Screen.Settings.route -> SettingsScreen(
                                    currentTheme = themePreference,
                                    onThemeChange = { newTheme ->
                                        mainViewModel.updateTheme(newTheme)
                                    }
                                )
                            }
                        }
                    }

                    // 挂载弹窗组件 (保持逻辑完全不变)
                    val homeViewModel: HomeViewModel = hiltViewModel()

                    if (showAddSheet) {
                        ManualAddSheet(
                            onDismiss = { showAddSheet = false },
                            onSave = { type, category, amount, remark, timestamp ->
                                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                                val icon = when (category) {
                                    "餐饮" -> "🍱"
                                    "交通" -> "🚗"
                                    "购物" -> "🛒"
                                    "娱乐" -> "🎮"
                                    "居住" -> "🏠"
                                    else -> "⚙️"
                                }
                                homeViewModel.addLedger(
                                    amount = parsedAmount,
                                    type = type,
                                    categoryName = category,
                                    categoryIcon = icon,
                                    timestamp = timestamp,
                                    note = remark
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}