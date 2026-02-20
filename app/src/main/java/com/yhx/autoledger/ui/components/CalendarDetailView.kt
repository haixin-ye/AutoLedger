package com.yhx.autoledger.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarExpenseView() {
    // 模拟分页状态，初始在当前月
    val pagerState = rememberPagerState(initialPage = 500) { 1000 }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // 月份切换器（Pager）
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(280.dp)
        ) { page ->
            CalendarGrid(page)
        }
    }
}

@Composable
fun CalendarGrid(page: Int) {
    // 模拟日历格子（7列）
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false
        ) {
            items(31) { index ->
                CalendarDayItem(day = (index + 1).toString(), expense = if (index % 3 == 0) "-58" else "")
            }
        }
    }
}

@Composable
fun CalendarDayItem(day: String, expense: String) {
    Column(
        modifier = Modifier.height(45.dp).padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(day, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (expense.isNotEmpty()) {
            Text(expense, fontSize = 9.sp, color = Color.Red.copy(alpha = 0.7f))
        }
    }
}