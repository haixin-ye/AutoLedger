package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.RefinedTransactionItem
import com.yhx.autoledger.TransactionData
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen() {
    // 1. 日历同步逻辑：以 2000年1月为基准
    val baseMonth = YearMonth.of(2000, 1)
    val today = YearMonth.now()
    val initialPage = (today.year - baseMonth.year) * 12 + (today.monthValue - baseMonth.monthValue)
    val pagerState = rememberPagerState(initialPage = initialPage) { 2400 }

    // 同步获取当前显示的月份
    val currentMonth = remember(pagerState.currentPage) {
        baseMonth.plusMonths(pagerState.currentPage.toLong())
    }

    var selectedCategory by remember { mutableStateOf<CategoryPercentage?>(null) }

    AnimatedContent(targetState = selectedCategory, label = "screen_transition") { category ->
        if (category == null) {
            MainDetailContent(currentMonth, pagerState) { selectedCategory = it }
        } else {
            CategoryDetailView(category) { selectedCategory = null }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainDetailContent(
    month: YearMonth,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onCategoryClick: (CategoryPercentage) -> Unit
) {
    // 模拟接口数据（后续只需替换此处的 List 即可）
    val mockCategories = listOf(
        CategoryPercentage("餐饮美食", "1250.00", 0.4f, "🍱", Color(0xFFFF7675)),
        CategoryPercentage("交通出行", "850.50", 0.25f, "🚗", Color(0xFF74EBD5)),
        CategoryPercentage("购物消费", "620.00", 0.2f, "🛒", Color(0xFFFAB1A0)),
        CategoryPercentage("其他支出", "529.50", 0.15f, "⚙️", Color(0xFF81ECEC))
    )

    LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        // A. 顶部标题与年月选择
        item {
            DetailTopBar(month)
        }

        // B. 真实日历滑动
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(280.dp).padding(horizontal = 16.dp)
            ) { page ->
                val pageMonth = YearMonth.of(2000, 1).plusMonths(page.toLong())
                CalendarGrid(pageMonth)
            }
        }

        // C. 数据总览（2x2 网格）
        item {
            DataOverviewSection(MonthlyStats("3,250", "8,000", "4,750", "116.0"))
        }

        // D. 优化后的分类饼图
        item {
            CategoryAnalysisChart(mockCategories)
        }

        // E. 分类块（带进度条和占比）
        items(mockCategories) { category ->
            CategoryDetailRow(category) { onCategoryClick(category) }
        }
    }
}

@Composable
fun DetailTopBar(month: YearMonth) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("账单明细", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Surface(
            modifier = Modifier.bounceClick().clickable { /* 弹出年月选择器接口 */ },
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(month.format(DateTimeFormatter.ofPattern("yyyy年 MM月")), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = AccentBlue)
            }
        }
    }
}

@Composable
fun CalendarGrid(month: YearMonth) {
    val firstDay = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp), userScrollEnabled = false) {
            items(firstDay) { Spacer(Modifier.size(40.dp)) }
            items(daysInMonth) { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(45.dp)) {
                    Text((day + 1).toString(), fontSize = 14.sp)
                    if ((day + 1) % 5 == 0) Text("-58", fontSize = 9.sp, color = Color.Red.copy(0.7f))
                }
            }
        }
    }
}

@Composable
fun DataOverviewSection(stats: MonthlyStats) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("数据总览", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                StatItem("支出", stats.totalExpense, Modifier.weight(1f))
                StatItem("收入", stats.totalIncome, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                StatItem("结余", stats.balance, Modifier.weight(1f))
                StatItem("日均支出", stats.dailyAvg, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text("¥$value", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}

@Composable
fun CategoryAnalysisChart(data: List<CategoryPercentage>) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 24.dp.toPx()
            val radius = size.minDimension / 2.2f
            var startAngle = -90f

            data.forEach { item ->
                val sweepAngle = item.percentage * 360f
                // 1. 绘制圆环（使用 Butt 解决圆头重叠问题）
                drawArc(
                    color = item.color.copy(alpha = 0.8f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // 2. 绘制引导线：从环体中心外缘开始
                val middleAngle = (startAngle + sweepAngle / 2) * (Math.PI / 180f).toFloat()
                val lineStartRadius = radius + strokeWidth / 2
                val lineEndRadius = lineStartRadius + 20.dp.toPx()

                val startOffset = Offset(center.x + lineStartRadius * cos(middleAngle), center.y + lineStartRadius * sin(middleAngle))
                val endOffset = Offset(center.x + lineEndRadius * cos(middleAngle), center.y + lineEndRadius * sin(middleAngle))

                drawLine(Color.LightGray.copy(0.5f), startOffset, endOffset, 1.dp.toPx())

                // 3. 绘制标签
                drawContext.canvas.nativeCanvas.drawText(
                    "${item.name} ${(item.percentage * 100).toInt()}%",
                    endOffset.x + (if (cos(middleAngle) > 0) 4.dp.toPx() else -60.dp.toPx()),
                    endOffset.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(255, (item.color.red * 255).toInt(), (item.color.green * 255).toInt(), (item.color.blue * 255).toInt())
                        textSize = 32f
                        isFakeBoldText = true
                    }
                )
                startAngle += sweepAngle
            }
        }
        Text("分类分析", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun CategoryDetailRow(category: CategoryPercentage, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).bounceClick().clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(category.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("¥${category.amount}", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            // 比例条可视化
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { category.percentage },
                    modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                    color = category.color,
                    trackColor = Color(0xFFF1F2F6)
                )
                Spacer(Modifier.width(12.dp))
                Text("${(category.percentage * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CategoryDetailView(category: CategoryPercentage, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("${category.name} 明细", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }

        // 使用与首页一致的账目块
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(5) {
                RefinedTransactionItem(TransactionData(category.name, category.icon, "25.00", category.color))
            }
        }
    }
}