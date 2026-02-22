package com.yhx.autoledger

// 别忘了在文件顶部引入必要的动画相关包
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.components.ManualAddSheet
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.screens.HomeScreen
import com.yhx.autoledger.ui.screens.SettingsScreen
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.ui.theme.AutoLedgerTheme
import com.yhx.autoledger.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var categoryDao: CategoryDao

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 3. 临时测试代码：强行读取一次数据库，触发 onCreate 回调和预设数据注入
        lifecycleScope.launch {
            categoryDao.getAllCategories().collect { categories ->
                Log.d("DB_TEST", "数据库被唤醒啦！当前有 ${categories.size} 个分类")
            }
        }

        setContent {
            AutoLedgerTheme {
                // ✨ 1. 定义页面的顺序列表（作为 Pager 的数据源）
                val tabOrder = remember {
                    listOf(Screen.Home, Screen.Detail, Screen.AI, Screen.Settings)
                }

                // ✨ 2. 初始化 Pager 状态 (管理当前滑到了哪一页)
                val pagerState = rememberPagerState(pageCount = { tabOrder.size })

                // ✨ 3. 协程作用域 (用于点击底部导航时，触发页面平滑滚动)
                val coroutineScope = rememberCoroutineScope()

                var showAddSheet by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        MainBottomBar(
                            // 动态获取当前滑到的页面 Route，传给底部导航栏让图标高亮
                            currentRoute = tabOrder[pagerState.currentPage].route,
                            onNavigate = { route ->
                                // 当用户点击底部图标时，找到目标索引，并触发平滑滚动
                                val targetIndex = tabOrder.indexOfFirst { it.route == route }
                                if (targetIndex != -1) {
                                    coroutineScope.launch {
                                        // 丝滑地滚动到目标页！
                                        pagerState.scrollToPage(targetIndex)
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        // ✨ 智能判断：第 0 页(首页) 和 第 1 页(明细页) 才显示加号
                        if (pagerState.currentPage == 0 || pagerState.currentPage == 1) {
                            FloatingActionButton(
                                onClick = { showAddSheet = true },
                                containerColor = AccentBlue,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.padding(bottom = 16.dp).bounceClick()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "手动记账", modifier = Modifier.size(28.dp))
                            }
                        }
                    },
                    containerColor = Color(0xFFF7F9FC)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // ✨ 4. 核心武器：HorizontalPager 完美接管手势与页面内容！
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->
                            // 根据当前的页码，渲染对应的屏幕
                            when (tabOrder[pageIndex].route) {
                                Screen.Home.route -> HomeScreen()
                                Screen.Detail.route -> DetailScreen()
                                Screen.AI.route -> AIScreen()
                                Screen.Settings.route -> SettingsScreen()
                            }
                        }
                    }

                    // ✨ 挂载弹窗组件 (这部分代码保持你原来的逻辑完全不变)
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