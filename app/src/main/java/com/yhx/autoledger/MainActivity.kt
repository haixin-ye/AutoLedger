package com.yhx.autoledger

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    if (showAddSheet) {
                        ManualAddSheet(
                            onDismiss = { showAddSheet = false },
                            onSave = { category, amount,remark ->
                                // TODO: 后续在这里对接后端 Room 数据库插入逻辑
                                println("手动保存：分类=$category, 金额=$amount, 备注=$remark")
                            }
                        )
                    }
                }
            }
        }
    }
}