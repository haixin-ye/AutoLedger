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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.screens.HomeScreen
import com.yhx.autoledger.ui.screens.SettingsScreen
import com.yhx.autoledger.ui.theme.AutoLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoLedgerTheme {
                var currentScreen by remember { mutableStateOf(Screen.Home.route) }

                Scaffold(
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
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
                }
            }
        }
    }
}