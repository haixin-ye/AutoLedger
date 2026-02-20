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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.components.DoubleCircleGauges
import com.yhx.autoledger.ui.components.MainBalanceCard
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.theme.AutoLedgerTheme
import com.yhx.autoledger.ui.theme.CategoryFood
import com.yhx.autoledger.ui.theme.CategoryOther
import com.yhx.autoledger.ui.theme.CategoryShop
import com.yhx.autoledger.ui.theme.CategoryTransport


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoLedgerTheme {
                // 状态记录：当前在哪一个页面
                var currentScreen by remember { mutableStateOf(Screen.Home.route) }

                Scaffold(
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
                    },
                    containerColor = Color(0xFFF7F9FC) // 浅蓝色底色
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // 页面切换动效
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            },
                            label = "screen_transition"
                        ) { targetRoute ->
                            when (targetRoute) {
                                // ✨ 这里是关键：调用你写的 HomeContent
                                Screen.Home.route -> HomeContent()

                                Screen.Detail.route -> DetailScreen()
                                Screen.AI.route -> AIScreen()
                                Screen.Settings.route -> "设置中心"
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 以下是具体的页面组件，放在类外面更加专业 ---

@Composable
fun HomeContent() {
    // 模拟数据结构：按天分组
    val groupedRecords = listOf(
        "2月20日 今天" to listOf(
            TransactionData("美团外卖", "🍱", "25.00", CategoryFood),
            TransactionData("滴滴打车", "🚗", "18.50", CategoryTransport)
        ),
        "2月19日 昨天" to listOf(
            TransactionData("超市购物", "🛒", "120.00", CategoryShop),
            TransactionData("移动话费", "📱", "50.00", CategoryOther)
        )
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // 1. 终极资产卡片
        item {
            MainBalanceCard("3,250", "5,000", "8,000", "4,750", "86.5")
        }

        // 2. 双圆形仪表盘
        item {
            DoubleCircleGauges(monthProgress = 0.65f, dayProgress = 0.42f)
        }

        // 3. 分类标题与按天分块列表
        groupedRecords.forEach { (date, items) ->
            item {
                Text(
                    date,
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
            }
            items(items) { data ->
                RefinedTransactionItem(data)
            }
        }
    }
}

// 数据类方便传递
data class TransactionData(val title: String, val icon: String, val amount: String, val color: Color)

@Composable
fun RefinedTransactionItem(data: TransactionData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).bounceClick(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(data.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(data.icon, fontSize = 22.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("12:30", fontSize = 12.sp, color = Color.Gray)
            }
            Text("-¥${data.amount}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Black)
        }
    }
}