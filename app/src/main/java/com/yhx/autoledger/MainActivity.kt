package com.yhx.autoledger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                var currentScreen by remember { mutableStateOf(Screen.Home.route) }
                var showAddSheet by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
                    },
                    floatingActionButton = {
                        // 智能判断：只在首页和明细页显示加号
                        if (currentScreen == Screen.Home.route || currentScreen == Screen.Detail.route) {
                            FloatingActionButton(
                                onClick = { showAddSheet = true },
                                containerColor = AccentBlue,
                                contentColor = Color.White,
                                shape = CircleShape, // 完美的正圆形
                                modifier = Modifier.padding(bottom = 16.dp).bounceClick() // 增加一点底部间距和点击动效
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "手动记账", modifier = Modifier.size(28.dp))
                            }
                        }
                    },
                    containerColor = Color(0xFFF7F9FC)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            },
                            label = "screen_transition"
                        ) { targetRoute ->
                            when (targetRoute) {
                                Screen.Home.route -> HomeScreen() // 引用抽离后的 HomeScreen
                                Screen.Detail.route -> DetailScreen()
                                Screen.AI.route -> AIScreen()
                                Screen.Settings.route -> SettingsScreen()
                            }

                        }
                    }

                    // ✨ 挂载弹窗组件
                    val homeViewModel: HomeViewModel = hiltViewModel()

                    if (showAddSheet) {
                        ManualAddSheet(
                            onDismiss = { showAddSheet = false },
                            onSave = { type,category, amount, remark ->
                                // 把字符串金额转为 Double
                                val parsedAmount = amount.toDoubleOrNull() ?: 0.0

                                // 简单匹配一下图标 (为了演示，你可以把这个提取成一个工具方法)
                                val icon = when (category) {
                                    "餐饮" -> "🍱"
                                    "交通" -> "🚗"
                                    "购物" -> "🛒"
                                    "娱乐" -> "🎮"
                                    "居住" -> "🏠"
                                    else -> "⚙️"
                                }

                                // 写入数据库！
                                homeViewModel.addLedger(
                                    amount = parsedAmount,
                                    type = type,
                                    categoryName = category,
                                    categoryIcon = icon,
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