package com.yhx.autoledger.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhx.autoledger.ui.components.DoubleCircleGauges
import com.yhx.autoledger.ui.components.MainBalanceCard
import com.yhx.autoledger.ui.components.RefinedTransactionItem
import com.yhx.autoledger.ui.components.TransactionData
import com.yhx.autoledger.ui.theme.CategoryFood
import com.yhx.autoledger.ui.theme.CategoryOther
import com.yhx.autoledger.ui.theme.CategoryShop
import com.yhx.autoledger.ui.theme.CategoryTransport

@Composable
fun HomeScreen() {
    // 模拟数据结构：按天分组 (后续将由 ViewModel 提供)
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