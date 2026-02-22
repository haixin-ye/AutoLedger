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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.components.RefinedTransactionItem
import com.yhx.autoledger.ui.components.TransactionData
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.viewmodel.DailyRecord
import com.yhx.autoledger.viewmodel.DetailViewModel
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(viewModel: DetailViewModel = hiltViewModel()) {
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val dailyRecordsMap by viewModel.dailyRecordsMap.collectAsState()
    val categoryPercentages by viewModel.categoryPercentages.collectAsState()
    val currentMonthLedgers by viewModel.currentMonthLedgers.collectAsState()

    val baseMonth = YearMonth.of(2000, 1)
    val today = YearMonth.now()
    val initialPage = (today.year - baseMonth.year) * 12 + (today.monthValue - baseMonth.monthValue)
    val pagerState = rememberPagerState(initialPage = initialPage) { 2400 }

    val scope = rememberCoroutineScope()
    var showMonthPicker by remember { mutableStateOf(false) }

    val currentMonth = remember(pagerState.currentPage) {
        baseMonth.plusMonths(pagerState.currentPage.toLong())
    }


    @Composable
    fun YearMonthPickerDialog(
        initialMonth: YearMonth,
        onConfirm: (YearMonth) -> Unit,
        onDismiss: () -> Unit
    ) {
        // 记录弹窗内部独立的状态
        var selectedYear by remember { mutableStateOf(initialMonth.year) }
        var selectedMonth by remember { mutableStateOf(initialMonth.monthValue) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. 年份切换 Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "上一年")
                        }
                        Text(
                            text = "$selectedYear 年",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2D3436)
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "下一年")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. 12个月份网格
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 一行4个月，共3行
                        modifier = Modifier.height(150.dp),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(12) { index ->
                            val month = index + 1
                            val isSelected = month == selectedMonth

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentBlue else Color(0xFFF0F4F8))
                                    .clickable { selectedMonth = month }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${month}月",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF2D3436)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. 底部操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(onClick = onDismiss) {
                            Text("取消", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Button(
                            onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }

    // ✨ 真正的日期选择弹窗
    if (showMonthPicker) {
        YearMonthPickerDialog(
            initialMonth = currentMonth, // 打开时默认选中当前页面显示的月份
            onConfirm = { selectedYearMonth ->
                // 计算选中的年月对应 Pager 的哪一页
                val targetPage = (selectedYearMonth.year - baseMonth.year) * 12 +
                        (selectedYearMonth.monthValue - baseMonth.monthValue)

                // 平滑滑动过去
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
                showMonthPicker = false // 关闭弹窗
            },
            onDismiss = {
                showMonthPicker = false // 取消关闭弹窗
            }
        )
    }



    LaunchedEffect(pagerState.currentPage) {
        val newOffset = pagerState.currentPage - initialPage
        viewModel.monthOffset.value = newOffset
    }

    var selectedCategoryInfo by remember { mutableStateOf<Pair<CategoryPercentage, Int>?>(null) }



    AnimatedContent(targetState = selectedCategoryInfo, label = "screen_transition") { info ->
        if (info == null) {
            MainDetailContent(
                currentMonth,
                pagerState,
                monthlyStats,
                dailyRecordsMap,
                categoryPercentages,
                onMonthClick = { showMonthPicker = true }, // 传入点击事件
                onCategoryClick = { cat, idx -> selectedCategoryInfo = cat to idx }
            )
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
    onMonthClick: () -> Unit,
    onCategoryClick: (CategoryPercentage, Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        // ✨ 需求 1：修改后的 TopBar
        item { DetailTopBar(month, onMonthClick) }

        item {
            // ✨ 需求 3：增强滑动体验
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(300.dp).padding(horizontal = 16.dp),
                pageSpacing = 16.dp
            ) { page ->
                // 计算当前页面的偏移量 (0.0 到 1.0)
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                // 根据偏移量计算缩放和透明度
                val alpha = 1f - Math.abs(pageOffset).coerceIn(0f, 0.6f)
                val scale = 1f - (Math.abs(pageOffset) * 0.08f)

                Box(modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }) {
                    val pageMonth = YearMonth.of(2000, 1).plusMonths(page.toLong())
                    CalendarGrid(pageMonth, dailyMap)
                }
            }
        }

        item { DataOverviewSection(stats) }

        if (categories.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("本月暂无记录 🍃", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            item { PremiumDonutChart(categories, stats.totalExpense) }
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
    // 这里的 1 代表周一，如果你的日历是以周日开头，需要处理这个偏移
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    // ✨ 需求 2：计算补全日期
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 1. 填充上个月的末尾日期
            items(firstDayOfWeek) { index ->
                val day = daysInPrevMonth - (firstDayOfWeek - index - 1)
                CalendarDayCell(day = day, record = null, isToday = false, isCurrentMonth = false)
            }

            // 2. 填充本月日期
            val today = Calendar.getInstance()
            val isThisMonth = month.year == today.get(Calendar.YEAR) && month.monthValue == (today.get(Calendar.MONTH) + 1)

            items(daysInMonth) { index ->
                val day = index + 1
                val isToday = isThisMonth && day == today.get(Calendar.DAY_OF_MONTH)
                CalendarDayCell(day = day, record = dailyMap[day], isToday = isToday, isCurrentMonth = true)
            }

            // 3. 填充下个月的起始日期（保证日历格子整齐，填充到 42 格即 6 行）
            val remainingCells = 42 - (firstDayOfWeek + daysInMonth)
            items(remainingCells) { index ->
                val day = index + 1
                CalendarDayCell(day = day, record = null, isToday = false, isCurrentMonth = false)
            }
        }
    }
}

@Composable
fun CalendarDayCell(day: Int, record: DailyRecord?, isToday: Boolean, isCurrentMonth: Boolean) {
    // ✨ 需求 2：非本月日期设为半透明灰色
    val textColor = if (isCurrentMonth) {
        if (isToday) Color(0xFF1976D2) else Color(0xFF2D3436)
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().height(46.dp).background(
            if (isToday) Color(0xFFDADDE0) else Color.Transparent,
            RoundedCornerShape(8.dp)
        )
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        /// 仅本月且有数据时显示
        if (isCurrentMonth && record != null && (record.expense > 0 || record.income > 0)) {
            val netAmount = record.income - record.expense
            Text(
                text = "${if(netAmount >= 0) "+" else "-"}${Math.abs(netAmount).toInt()}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = (if (netAmount >= 0) Color(0xFF34A853) else Color(0xFFE53935)).copy(alpha = 0.7f),
                maxLines = 1
            )
        } else {
            // 占位空间也要缩小
            Spacer(modifier = Modifier.height(9.dp))
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
fun DetailTopBar(month: YearMonth, onMonthClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("账单明细", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier.bounceClick().clickable { onMonthClick() },
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
                Spacer(Modifier.width(4.dp))
                // ✨ 需求 1：改为向下箭头，表示可点击下拉
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AccentBlue
                )
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



