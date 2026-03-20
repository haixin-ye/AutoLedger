package com.yhx.autoledger.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    items: List<Int>,
    initialItem: Int,
    itemHeight: Dp = 50.dp,
    onItemSelected: (Int) -> Unit
) {
    val virtualCount = Int.MAX_VALUE
    val actualCount = items.size

    // 算出居中的初始位置
    val middle = virtualCount / 2
    val baseIndex = middle - (middle % actualCount)
    val initialIndex = items.indexOf(initialItem).takeIf { it != -1 } ?: 0

    // 因为容器高度是 3 个格子，你想让目标在第 2 个格子(中间)，那么第 1 个格子(顶部)的索引就是 目标-1
    val startIndex = baseIndex + initialIndex - 1

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // ✨ 核心魔法：绝对几何中心探测法 (彻底解决错位问题)
    val currentCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf -1

            // 算出整个可视区域的绝对中心 Y 坐标
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            // 遍历当前屏幕上可见的这几个格子，看看谁的中心点离 viewportCenter 最近
            val closestItem = visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + (item.size / 2)
                kotlin.math.abs(itemCenter - viewportCenter)
            }
            closestItem?.index ?: -1
        }
    }

    // 监听滚动，向外回调当前正中心的那个值
    LaunchedEffect(currentCenterIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && currentCenterIndex != -1) {
            val realIndex = currentCenterIndex % actualCount
            val safeIndex = if (realIndex < 0) realIndex + actualCount else realIndex
            onItemSelected(items[safeIndex])
        }
    }

    Box(
        modifier = Modifier
            .width(60.dp) // 宽度调窄，呈现精致感
            .height(itemHeight * 3), // 高度严格等于 3 个 Item
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior, // 自带吸附效果，松手自动对齐
            modifier = Modifier.fillMaxSize()
        ) {
            items(virtualCount) { index ->
                val realIndex = index % actualCount
                val safeIndex = if (realIndex < 0) realIndex + actualCount else realIndex
                val value = items[safeIndex]

                // 这个格子是否刚好落在绝对中心点上
                val isCenter = index == currentCenterIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", value),
                        // ✨ 选中项：大号字体 + 你的品牌高亮色
                        fontSize = if (isCenter) 24.sp else 16.sp,
                        fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCenter) AppDesignSystem.colors.brandAccent else AppDesignSystem.colors.textTertiary,
                        // ✨ 非选中项：缩小 + 颜色变浅 + 半透明，营造出 iOS 的 3D 透视滚轮感
                        modifier = Modifier.alpha(if (isCenter) 1f else 0.4f)
                    )
                }
            }
        }
    }
}