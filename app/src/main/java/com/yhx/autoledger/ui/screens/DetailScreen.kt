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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.components.RefinedTransactionItem
import com.yhx.autoledger.ui.components.TransactionData
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.viewmodel.DailyRecord
import com.yhx.autoledger.viewmodel.DetailViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

// 在文件顶部定义高级色板
private val PremiumColors = listOf(
    Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4)), // 清新薄荷 -> 晴空蓝
    Pair(Color(0xFFA18CD1), Color(0xFFFBC2EB)), // 梦幻紫 -> 浅樱粉
    Pair(Color(0xFFFFECD2), Color(0xFFFCB69F)), // 活力蜜桃
    Pair(Color(0xFF4FACFE), Color(0xFF00F2FE)), // 科技亮蓝
    Pair(Color(0xFFF6D365), Color(0xFFFDA085)), // 暖阳橙黄
    Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC)), // 晚霞灰紫
    Pair(Color(0xFFFFAA85), Color(0xFFB3315F))  // 树莓红
)

fun getPremiumBrush(index: Int): Brush {
    val colors = PremiumColors[index % PremiumColors.size]
    return Brush.linearGradient(listOf(colors.first, colors.second))
}

// ✨ 新增：提取该渐变系列的主色调，供给下方的图标使用
fun getPremiumBaseColor(index: Int): Color {
    return PremiumColors[index % PremiumColors.size].first
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(viewModel: DetailViewModel = hiltViewModel()) {
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val dailyRecordsMap by viewModel.dailyRecordsMap.collectAsState() // 接收新的双数据 Map
    val categoryPercentages by viewModel.categoryPercentages.collectAsState()
    val currentMonthLedgers by viewModel.currentMonthLedgers.collectAsState()

    val baseMonth = YearMonth.of(2000, 1)
    val today = YearMonth.now()
    val initialPage = (today.year - baseMonth.year) * 12 + (today.monthValue - baseMonth.monthValue)
    val pagerState = rememberPagerState(initialPage = initialPage) { 2400 }

    // ✨ 修复 1 & 2：移除 <=0 的拦截，允许未来/空月份彻底刷新为 0.0 数据
    LaunchedEffect(pagerState.currentPage) {
        val newOffset = pagerState.currentPage - initialPage
        viewModel.monthOffset.value = newOffset
    }

    val currentMonth =
        remember(pagerState.currentPage) { baseMonth.plusMonths(pagerState.currentPage.toLong()) }
    var selectedCategoryInfo by remember { mutableStateOf<Pair<CategoryPercentage, Int>?>(null) }

    AnimatedContent(targetState = selectedCategoryInfo, label = "screen_transition") { info ->
        if (info == null) {
            MainDetailContent(
                currentMonth,
                pagerState,
                monthlyStats,
                dailyRecordsMap,
                categoryPercentages
            ) { cat, idx ->
                selectedCategoryInfo = cat to idx
            }
        } else {
            CategoryDetailView(
                info.first,
                info.second,
                currentMonthLedgers
            ) { selectedCategoryInfo = null }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainDetailContent(
    month: YearMonth,
    pagerState: androidx.compose.foundation.pager.PagerState,
    stats: MonthlyStats,
    dailyMap: Map<Int, DailyRecord>,
    categories: List<CategoryPercentage>,
    onCategoryClick: (CategoryPercentage, Int) -> Unit
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF7F9FC))) {

        // ✨ 恢复 1：顶部的月份切换标题
        item { DetailTopBar(month) }

        // ✨ 恢复 2：核心的日历滑动组件
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(300.dp).padding(horizontal = 16.dp)
            ) { page ->
                val pageMonth = YearMonth.of(2000, 1).plusMonths(page.toLong())
                CalendarGrid(pageMonth, dailyMap)
            }
        }

        // ✨ 恢复 3：收支结余数据总览
        item { DataOverviewSection(stats) }

        if (categories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("本月暂无记录 🍃", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            // 您的圆环图
            item { PremiumDonutChart(categories, stats.totalExpense) }

            // 分类明细列表
            itemsIndexed(categories) { index, category ->
                CategoryDetailRow(category, index) { onCategoryClick(category, index) }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

//圆环图
@Composable
fun PremiumDonutChart(data: List<CategoryPercentage>, totalExpense: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(vertical = 16.dp)
    ) {
        // ✨ 将尺寸放大一点，配合更粗的线条显得更饱满
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidthPx = 22.dp.toPx()

            // 真实的绘制半径需要减去线宽的一半，防止弧线超出 Canvas 边界被裁切
            val radius = (size.minDimension - strokeWidthPx) / 2f

            // ----------------------------------------------------
            // 🧠 导师级黑科技：利用圆周率精准计算 StrokeCap.Round 产生的溢出角度
            // ----------------------------------------------------
            val circumference = 2f * Math.PI.toFloat() * radius
            val capAngle = (strokeWidthPx / circumference) * 360f

            // 我们想要的视觉真实缝隙（2度）
            val visualGapAngle = 2f

            // 总偏移角度 = 圆角溢出角度 + 真实缝隙
            val totalOffsetAngle = capAngle + visualGapAngle

            // 1. 绘制底层的高级浅色轨道（增加图表的厚重感）
            drawCircle(
                color = Color(0xFFF1F3F6),
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            var currentStartAngle = -90f

            // ✨ 判断如果只有一个数据，直接画一个完美的闭合整圆
            if (data.size == 1) {
                drawArc(
                    brush = getPremiumBrush(0),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx), // 不需要 Cap，因为首尾相接了
                    topLeft = androidx.compose.ui.geometry.Offset(
                        strokeWidthPx / 2f,
                        strokeWidthPx / 2f
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                )
            } else {
                // 如果有多个数据，走原来的留缝隙逻辑
                data.forEachIndexed { index, item ->
                    val rawSweep = item.percentage * 360f
                    if (rawSweep > totalOffsetAngle) {
                        val actualSweep = rawSweep - totalOffsetAngle
                        val actualStart = currentStartAngle + (totalOffsetAngle / 2f)
                        drawArc(
                            brush = getPremiumBrush(index),
                            startAngle = actualStart,
                            sweepAngle = actualSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                strokeWidthPx / 2f,
                                strokeWidthPx / 2f
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                        )
                    } else if (rawSweep > 0f) {
                        drawArc(
                            brush = getPremiumBrush(index),
                            startAngle = currentStartAngle + (visualGapAngle / 2f),
                            sweepAngle = maxOf(0.5f, rawSweep - visualGapAngle),
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                strokeWidthPx / 2f,
                                strokeWidthPx / 2f
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                        )
                    }
                    currentStartAngle += rawSweep
                }
            }
        }

        // 3. 极简优雅的中心排版
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "本月总支出",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp // 增加字间距，提升精致感
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 4.dp, end = 2.dp)
                )
                Text(
                    text = totalExpense,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1F2937),
                    letterSpacing = (-0.5).sp // 数字微缩字间距，显得更紧凑有力
                )
            }
        }
    }
}

// ✨ 日历显示支出和收入
@Composable
fun CalendarGrid(month: YearMonth, dailyMap: Map<Int, DailyRecord>) {
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val today = Calendar.getInstance()
    val isCurrentMonth =
        month.year == today.get(Calendar.YEAR) && month.monthValue == (today.get(Calendar.MONTH) + 1)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // 星期抬头
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(6.dp) // 增加行间距
        ) {
            items(firstDayOfWeek) { Spacer(Modifier.size(40.dp)) }
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val record = dailyMap[day]
                val isToday = isCurrentMonth && day == todayDay

                CalendarDayCell(day = day, record = record, isToday = isToday)
            }
        }
    }
}

@Composable
fun CalendarDayCell(day: Int, record: DailyRecord?, isToday: Boolean) {
    val exp = record?.expense ?: 0.0
    val inc = record?.income ?: 0.0
    val netAmount = inc - exp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            // 今天的高亮依然保留一个极淡的底色
            .background(
                if (isToday) Color(0xFFDADDE0) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
    ) {
        // 1. 日期
        Text(
            text = day.toString(),
            fontSize = 15.sp,
            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium,
            color = if (isToday) Color(0xFF1976D2) else Color(0xFF2D3436)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 2. 极简净收支数字
        if (exp > 0.0 || inc > 0.0) {
            val isIncome = netAmount > 0
            // 采用更柔和、饱和度更低的红绿色，避免刺眼
            val themeColor = if (isIncome) Color(0xFF34A853) else Color(0xFFE53935)
            val prefix = if (isIncome) "+" else "-"

            Text(
                text = "${prefix}${String.format("%.0f", Math.abs(netAmount))}",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold, // 极小字号必须配合超粗体
                color = themeColor,
                maxLines = 1
            )
        } else {
            Spacer(modifier = Modifier.height(11.dp)) // 占位，防止排版跳动
        }
    }
}


@Composable
fun CategoryDetailRow(category: CategoryPercentage, index: Int, onClick: () -> Unit) { // ✨ 这里加上 index: Int
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .bounceClick()
            .clickable { onClick() },
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
                // 手写渐变进度条
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F2F6))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.percentage)
                            .fillMaxHeight()
                            .background(getPremiumBrush(index)) // ✨ 这里就能正确识别到 index 了！
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${(category.percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}



@Composable
fun CategoryDetailView(
    category: CategoryPercentage,
    categoryIndex: Int,
    allLedgers: List<com.yhx.autoledger.data.entity.LedgerEntity>,
    onBack: () -> Unit
) {
    val categoryLedgers = remember(category, allLedgers) {
        allLedgers.filter { it.categoryName == category.name }
    }

    Column(Modifier
        .fillMaxSize()
        .background(Color(0xFFF7F9FC))) {

        // ✨ 恢复 4：二级明细页的返回按钮和标题栏
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text("${category.name} 明细", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(categoryLedgers) { ledger ->
                RefinedTransactionItem(
                    data = TransactionData(
                        title = ledger.categoryName,
                        icon = ledger.categoryIcon ?: "🏷️",
                        amount = "- ¥${String.format("%.2f", ledger.amount)}",
                        color = getPremiumBaseColor(categoryIndex),
                        originalLedger = ledger
                    ),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun DetailTopBar(month: YearMonth) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "账单明细",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        Surface(
            modifier = Modifier
                .bounceClick()
                .clickable { /* 弹出年月选择器接口 */ },
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    month.format(DateTimeFormatter.ofPattern("yyyy年 MM月")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = AccentBlue)
            }
        }
    }
}

@Composable
fun DataOverviewSection(stats: MonthlyStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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



