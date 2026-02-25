package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.models.MonthlyStats
import com.yhx.autoledger.ui.components.CalendarGrid
import com.yhx.autoledger.ui.components.CategoryDetailView
import com.yhx.autoledger.ui.components.DailyTrendChart
import com.yhx.autoledger.ui.components.DataOverviewSection
import com.yhx.autoledger.ui.components.DetailTopBar
import com.yhx.autoledger.ui.components.PremiumBlockCard
import com.yhx.autoledger.ui.components.PremiumDonutChart
import com.yhx.autoledger.ui.components.YearMonthPickerDialog
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.components.getPremiumBrush
import com.yhx.autoledger.ui.theme.AppTheme
import com.yhx.autoledger.viewmodel.DailyRecord
import com.yhx.autoledger.viewmodel.DetailViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth

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

    if (showMonthPicker) {
        YearMonthPickerDialog(
            initialMonth = currentMonth,
            onConfirm = { selectedYearMonth ->
                val targetPage = (selectedYearMonth.year - baseMonth.year) * 12 +
                        (selectedYearMonth.monthValue - baseMonth.monthValue)

                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
                showMonthPicker = false
            },
            onDismiss = {
                showMonthPicker = false
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
                onMonthClick = { showMonthPicker = true },
                onCategoryClick = { cat, idx -> selectedCategoryInfo = cat to idx }
            )
        } else {
            CategoryDetailView(
                category = info.first,
                categoryIndex = info.second,
                allLedgers = currentMonthLedgers,
                onBack = { selectedCategoryInfo = null },
                onSaveLedger = { updatedLedger -> viewModel.updateLedger(updatedLedger) },
                onDeleteLedger = { deletedLedger -> viewModel.deleteLedger(deletedLedger) }
            )
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
    onCategoryClick: (CategoryPercentage, Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val budget by viewModel.monthlyBudget.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.appBackground) // ✅ 完美复用
    ) {
        item { DetailTopBar(month, onMonthClick) }

        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(300.dp)
                    .padding(horizontal = 16.dp),
                pageSpacing = 16.dp
            ) { page ->
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

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

        item {
            PremiumBlockCard {
                DataOverviewSection(stats = stats, budget = budget)
            }
        }

        item {
            PremiumBlockCard {
                DailyTrendChart(month, dailyMap, budget)
            }
        }

        if (categories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("本月暂无记录 🍃", color = AppTheme.colors.textSecondary, fontSize = 16.sp)
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
        color = AppTheme.colors.cardBackground, // ✅ 卡片底色
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))

                // ✨ 修复点 1：显式指定分类名称的主文字颜色
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                // ✨ 修复点 2：显式指定金额的主文字颜色
                Text(
                    text = "¥${category.amount}",
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
                        .background(AppTheme.colors.surfaceVariant) // ✅ 进度条底槽色
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.percentage)
                            .fillMaxHeight()
                            // ✨ 完美获取在 AppDesignSystem 里配好的黑金渐变色
                            .background(getPremiumBrush(index))
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${(category.percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary // ✅ 进度百分比文字色
                )
            }
        }
    }
}


@Composable
fun StatItem(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
        Text("¥$value", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTheme.colors.textPrimary)
    }
}