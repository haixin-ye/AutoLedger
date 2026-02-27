package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.components.*
import com.yhx.autoledger.ui.theme.AppTheme
import com.yhx.autoledger.ui.theme.CategoryFood
import com.yhx.autoledger.ui.theme.CategoryOther
import com.yhx.autoledger.ui.theme.CategoryShop
import com.yhx.autoledger.ui.theme.CategoryTransport
import com.yhx.autoledger.viewmodel.DailyRecord
import com.yhx.autoledger.viewmodel.DetailViewModel
import com.yhx.autoledger.viewmodel.MonthlyRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// 路由状态管理
sealed class DetailRoute {
    object Main : DetailRoute()
    data class Category(val category: CategoryPercentage, val index: Int) : DetailRoute()
    data class Daily(val day: Int, val month: YearMonth) : DetailRoute()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(viewModel: DetailViewModel = hiltViewModel()) {
    var isYearView by remember { mutableStateOf(false) }

    // --- 状态获取 ---
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val dailyRecordsMap by viewModel.dailyRecordsMap.collectAsState()
    val categoryPercentages by viewModel.categoryPercentages.collectAsState()
    val currentMonthLedgers by viewModel.currentMonthLedgers.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val yearlyBudget by viewModel.yearlyBudget.collectAsState()

    val yearlyStats by viewModel.yearlyStats.collectAsState(initial = MonthlyStats("0.0", "0.0", "0.0", "0.0"))
    val yearlyCategoryPercentages by viewModel.yearlyCategoryPercentages.collectAsState(initial = emptyList())
    val currentYearLedgers by viewModel.currentYearLedgers.collectAsState(initial = emptyList())
    val yearlyMonthlyRecordsMap by viewModel.yearlyMonthlyRecordsMap.collectAsState(initial = emptyMap())

    val baseMonth = YearMonth.of(2000, 1)
    val today = YearMonth.now()
    val initialPage = (today.year - baseMonth.year) * 12 + (today.monthValue - baseMonth.monthValue)
    val pagerState = rememberPagerState(initialPage = initialPage) { 2400 }

    var selectedYear by remember { mutableStateOf(today.year) }
    val scope = rememberCoroutineScope()
    var showMonthPicker by remember { mutableStateOf(false) }

    val currentMonth = remember(pagerState.currentPage) {
        baseMonth.plusMonths(pagerState.currentPage.toLong())
    }

    // ✨ 悬浮提示框状态
    var topToastMessage by remember { mutableStateOf("") }
    var toastTrigger by remember { mutableIntStateOf(0) }

    // 监听触发器，2秒后自动隐藏提示
    LaunchedEffect(toastTrigger) {
        if (topToastMessage.isNotEmpty()) {
            delay(2000)
            topToastMessage = ""
        }
    }

    if (showMonthPicker) {
        YearMonthPickerDialog(
            initialMonth = currentMonth,
            onConfirm = { selectedYearMonth ->
                val targetPage = (selectedYearMonth.year - baseMonth.year) * 12 +
                        (selectedYearMonth.monthValue - baseMonth.monthValue)
                scope.launch { pagerState.animateScrollToPage(targetPage) }
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        val newOffset = pagerState.currentPage - initialPage
        viewModel.monthOffset.value = newOffset
    }

    LaunchedEffect(selectedYear) {
        viewModel.setYear(selectedYear)
    }

    var currentRoute by remember { mutableStateOf<DetailRoute>(DetailRoute.Main) }

    // ✨ 核心层级包裹：Box 必须包在最外面，保证提示框能悬浮
    Box(modifier = Modifier.fillMaxSize()) {

        // 主体页面内容
        AnimatedContent(targetState = currentRoute, label = "screen_transition") { route ->
            when (route) {
                is DetailRoute.Main -> {
                    val haptic = LocalHapticFeedback.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.appBackground)
                    ) {
                        ViewToggleSegment(
                            isYearView = isYearView,
                            onToggle = { isYearView = it }
                        )

                        // ✨ 丝滑的年月切换过渡动画
                        Crossfade(
                            targetState = isYearView,
                            animationSpec = androidx.compose.animation.core.tween(400),
                            label = "view_mode_transition"
                        ) { targetIsYearView ->
                            if (!targetIsYearView) {
                                // === 🌙 月视图内容 ===
                                MainDetailContent(
                                    currentMonth = currentMonth,
                                    pagerState = pagerState,
                                    stats = monthlyStats,
                                    dailyMap = dailyRecordsMap,
                                    categories = categoryPercentages,
                                    budget = monthlyBudget,
                                    onMonthClick = { showMonthPicker = true },
                                    onCategoryClick = { cat, idx -> currentRoute = DetailRoute.Category(cat, idx) },
                                    onDayClick = { day -> currentRoute = DetailRoute.Daily(day, currentMonth) }
                                )
                            } else {
                                // === ☀️ 年视图内容 ===
                                YearDetailContent(
                                    year = selectedYear,
                                    stats = yearlyStats,
                                    categories = yearlyCategoryPercentages,
                                    monthlyMap = yearlyMonthlyRecordsMap,
                                    budget = yearlyBudget,
                                    onYearChange = { selectedYear = it },
                                    onCategoryClick = { cat, idx -> currentRoute = DetailRoute.Category(cat, idx) },
                                    onMonthClick = { clickedMonth ->
                                        // 1. 触发长按震动反馈
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                        // 2. 触发顶部高级悬浮提示
                                        topToastMessage = "已为您切换至 ${selectedYear}年${clickedMonth}月"
                                        toastTrigger++

                                        // 3. 计算并后台切换月视图 Pager
                                        val targetYearMonth = YearMonth.of(selectedYear, clickedMonth)
                                        val targetPage = (targetYearMonth.year - baseMonth.year) * 12 +
                                                (targetYearMonth.monthValue - baseMonth.monthValue)

                                        scope.launch {
                                            pagerState.scrollToPage(targetPage)
                                            isYearView = false // 切回月视图
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is DetailRoute.Category -> {
                    CategoryDetailView(
                        category = route.category,
                        categoryIndex = route.index,
                        allLedgers = if (isYearView) currentYearLedgers else currentMonthLedgers,
                        onBack = { currentRoute = DetailRoute.Main },
                        onSaveLedger = { viewModel.updateLedger(it) },
                        onDeleteLedger = { viewModel.deleteLedger(it) }
                    )
                }
                is DetailRoute.Daily -> {
                    DailyDetailView(
                        day = route.day,
                        month = route.month,
                        allLedgers = currentMonthLedgers,
                        onBack = { currentRoute = DetailRoute.Main },
                        onSaveLedger = { viewModel.updateLedger(it) },
                        onDeleteLedger = { viewModel.deleteLedger(it) }
                    )
                }
            }
        }

        // ✨ 这个组件必须在 AnimatedContent 下面！这样它才会盖在所有内容上面
        AnimatedVisibility(
            visible = topToastMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp) // 避开刘海屏和状态栏
                .padding(horizontal = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppTheme.colors.cardBackground,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Celebration,
                        contentDescription = null,
                        tint = AppTheme.colors.brandAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topToastMessage,
                        color = AppTheme.colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ======================= 下方的组件均保持不变 =======================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainDetailContent(
    currentMonth: YearMonth,
    pagerState: androidx.compose.foundation.pager.PagerState,
    stats: MonthlyStats,
    dailyMap: Map<Int, DailyRecord>,
    categories: List<CategoryPercentage>,
    budget: Double,
    onMonthClick: () -> Unit,
    onCategoryClick: (CategoryPercentage, Int) -> Unit,
    onDayClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { DetailTopBar(currentMonth, onMonthClick) }
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(300.dp)
                    .padding(horizontal = 16.dp),
                pageSpacing = 16.dp
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val alpha = 1f - Math.abs(pageOffset).coerceIn(0f, 0.6f)
                val scale = 1f - (Math.abs(pageOffset) * 0.08f)

                Box(modifier = Modifier.graphicsLayer {
                    this.alpha = alpha; this.scaleX = scale; this.scaleY = scale
                }) {
                    CalendarGrid(
                        month = YearMonth.of(2000, 1).plusMonths(page.toLong()),
                        dailyMap = dailyMap,
                        onDayClick = onDayClick
                    )
                }
            }
        }
        item { PremiumBlockCard { DataOverviewSection(stats = stats, budget = budget) } }
        item { PremiumBlockCard { DailyTrendChart(currentMonth, dailyMap, budget) } }
        item { SectionTitle("账单明细") }

        if (categories.isEmpty()) {
            item { EmptyDataView() }
        } else {
            item { PremiumDonutChart(categories, stats.totalExpense) }
            itemsIndexed(categories) { index, category ->
                CategoryDetailRow(category, index) { onCategoryClick(category, index) }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun DailyDetailView(
    day: Int,
    month: YearMonth,
    allLedgers: List<LedgerEntity>,
    onBack: () -> Unit,
    onSaveLedger: (LedgerEntity) -> Unit,
    onDeleteLedger: (LedgerEntity) -> Unit
) {
    val dayLedgers = remember(allLedgers, day, month) {
        allLedgers.filter { ledger ->
            val cal = Calendar.getInstance().apply { timeInMillis = ledger.timestamp }
            cal.get(Calendar.DAY_OF_MONTH) == day &&
                    cal.get(Calendar.MONTH) + 1 == month.monthValue &&
                    cal.get(Calendar.YEAR) == month.year
        }.sortedByDescending { it.timestamp }
    }

    var selectedLedger by remember { mutableStateOf<LedgerEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.appBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowLeft,
                    contentDescription = "返回",
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "${month.monthValue}月${day}日 账单明细",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (dayLedgers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("这一天没有记账哦 🍃", color = AppTheme.colors.textSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dayLedgers) { ledger ->
                    val displayData = remember(ledger) {
                        val color = when (ledger.categoryName) {
                            "餐饮" -> CategoryFood
                            "交通" -> CategoryTransport
                            "购物" -> CategoryShop
                            else -> CategoryOther
                        }
                        val absVal = String.format(Locale.getDefault(), "%.2f", abs(ledger.amount))
                        val displayAmount = if (ledger.type == 0) "- ¥$absVal" else "+ ¥$absVal"

                        TransactionData(
                            title = ledger.note.ifBlank { ledger.categoryName },
                            icon = ledger.categoryIcon,
                            amount = displayAmount,
                            color = color,
                            originalLedger = ledger
                        )
                    }

                    RefinedTransactionItem(
                        data = displayData,
                        onClick = { selectedLedger = ledger }
                    )
                }
            }
        }
    }

    if (selectedLedger != null) {
        EditLedgerSheet(
            initialLedger = selectedLedger!!,
            onDismiss = { selectedLedger = null },
            onSave = {
                onSaveLedger(it)
                selectedLedger = null
            },
            onDelete = {
                onDeleteLedger(it)
                selectedLedger = null
            }
        )
    }
}

// ✨ 高级的胶囊 Segmented Control 切换器
@Composable
fun ViewToggleSegment(isYearView: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.surfaceVariant), // 使用全局浅灰槽
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (!isYearView) AppTheme.colors.cardBackground else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "月度",
                color = if (!isYearView) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                fontWeight = if (!isYearView) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isYearView) AppTheme.colors.cardBackground else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "年度",
                color = if (isYearView) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                fontWeight = if (isYearView) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

// =======================================================
// ✨ 2. 修改月视图排版：增加“账单明细”标题
// =======================================================


// =======================================================
// ✨ 2. 年视图内容排版：与 MainDetailContent 完全对齐
// =======================================================
@Composable
fun YearDetailContent(
    year: Int,
    stats: MonthlyStats,
    categories: List<CategoryPercentage>,
    monthlyMap: Map<Int, MonthlyRecord>,
    budget: Double,
    onYearChange: (Int) -> Unit,
    onCategoryClick: (CategoryPercentage, Int) -> Unit,
    onMonthClick: (Int) -> Unit // ✨ 1. 新增接收参数
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // 第一层：带有“账单明细”的顶部栏
        item { YearSelectorTopBar(year, onYearChange) }

        // 第二层：全新封装且高度固定的热力宫格 (外部加16dp边距与月视图日历对齐)
        item {
            // ✨ 2. 将点击事件传递给热力图组件
            YearlyCalendarGrid(
                year = year,
                monthlyMap = monthlyMap,
                onMonthClick = onMonthClick
            )
        }

        // 第三层：数据总览
        // 传入 isYearView = true 触发文案变更为“本年累计”
        item {
            PremiumBlockCard {
                DataOverviewSection(
                    stats = stats,
                    budget = budget,
                    isYearView = true // ✨ 触发 "本年" 文案！
                )
            }
        }

        // 第四层：月趋势图 (如果你想保持结构一样，这里调用你刚才写的 MonthlyTrendChart)
        item {
            PremiumBlockCard {
                MonthlyTrendChart(
                    year = year,
                    monthlyMap = monthlyMap,
                    budget = budget
                )
            }
        }

        // 第五层：分类数据
        if (categories.isEmpty()) {
            item { EmptyDataView() }
        } else {
            item { PremiumDonutChart(categories, stats.totalExpense) }
            itemsIndexed(categories) { index, category ->
                CategoryDetailRow(category, index) { onCategoryClick(category, index) }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CategoryDetailRow(category: CategoryPercentage, index: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .bounceClick()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        color = AppTheme.colors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    category.name,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "¥${category.amount}",
                    fontWeight = FontWeight.Black,
                    color = AppTheme.colors.textPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.percentage)
                            .fillMaxHeight()
                            .background(getPremiumBrush(index))
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${(category.percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
        Text(
            "¥$value",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = AppTheme.colors.textPrimary
        )
    }
}

@Composable
fun EmptyDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentAlignment = Alignment.Center
    ) {
        Text("暂无记录 🍃", color = AppTheme.colors.textSecondary, fontSize = 16.sp)
    }
}

// =======================================================
// ✨ 1. 新增：统一的小标题组件 (放在 DetailScreen.kt 底部)
// =======================================================
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.textPrimary,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
    )
}

// =======================================================
// ✨ 1. 年度顶部栏：完全复刻 DetailTopBar 的 UI
// =======================================================
@Composable
fun YearSelectorTopBar(year: Int, onYearChange: (Int) -> Unit) {
    // 提取颜色变量
    val textPrimary = AppTheme.colors.textPrimary
    val brandAccent = AppTheme.colors.brandAccent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp), // ✨ 与 DetailTopBar 边距像素级对齐
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "账单明细", // ✨ 标题文案与位置完全同步
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = brandAccent.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ... (Icon 切换年份逻辑，确保 InteractionSource 放在 Composable 作用域内) ...
                val interactionSourceLeft = remember { MutableInteractionSource() }
                Icon(
                    Icons.Default.KeyboardArrowLeft, null, tint = brandAccent,
                    modifier = Modifier.clickable(
                        interactionSourceLeft,
                        null
                    ) { onYearChange(year - 1) }
                )
                Text(
                    "${year}年",
                    Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = brandAccent
                )
                val interactionSourceRight = remember { MutableInteractionSource() }
                Icon(
                    Icons.Default.KeyboardArrowRight, null, tint = brandAccent,
                    modifier = Modifier.clickable(
                        interactionSourceRight,
                        null
                    ) { onYearChange(year + 1) }
                )
            }
        }
    }
}
