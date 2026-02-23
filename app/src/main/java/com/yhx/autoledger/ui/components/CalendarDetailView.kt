package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.viewmodel.DailyRecord
import java.time.YearMonth
import java.util.Calendar

// ✨ 日历显示支出和收入
@Composable
fun CalendarGrid(month: YearMonth, dailyMap: Map<Int, DailyRecord>) {
    // 这里的 1 代表周一，如果你的日历是以周日开头，需要处理这个偏移
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    // ✨ 需求 2：计算补全日期
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
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
            val isThisMonth =
                month.year == today.get(Calendar.YEAR) && month.monthValue == (today.get(Calendar.MONTH) + 1)

            items(daysInMonth) { index ->
                val day = index + 1
                val isToday = isThisMonth && day == today.get(Calendar.DAY_OF_MONTH)
                CalendarDayCell(
                    day = day,
                    record = dailyMap[day],
                    isToday = isToday,
                    isCurrentMonth = true
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(
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
                text = "${if (netAmount >= 0) "+" else "-"}${Math.abs(netAmount).toInt()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = (if (netAmount >= 0) Color(0xFF34A853) else Color(0xFFE53935)).copy(alpha = 0.7f),
                maxLines = 1
            )
        } else {
            // ✨ 核心修改点：用透明的 Text 替换 Spacer(9.dp)
            // 字体大小和粗细保持一致，确保高度完全相同
            Text(
                text = "0", // 任意占位字符
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Transparent, // 设置为透明，肉眼不可见
                maxLines = 1
            )
        }
    }
}

@Composable
fun DetailTopBar(month: YearMonth, onMonthClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp), // 增加上下的呼吸感
        horizontalArrangement = Arrangement.SpaceBetween, // 完美将两者推向两端
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ================= 左侧：重量级大标题 =================
        Text(
            text = "账单明细",
            fontSize = 28.sp, // ✨ 超大字号，确立页面层级
            fontWeight = FontWeight.Black,
            color = Color(0xFF1D1D1F), // 苹果极简深色
            letterSpacing = 1.sp
        )

        // ================= 右侧：高显眼度胶囊按钮 =================
        Surface(
            shape = RoundedCornerShape(20.dp), // 胶囊形状
            color = AccentBlue.copy(alpha = 0.1f), // ✨ 极淡的主题蓝底色，不刺眼但极度显眼
            modifier = Modifier.bounceClick() // 如果你有提取 bounceClick 动画就加上，没有可以去掉
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // 去除原生自带的方形波纹，保持胶囊的纯粹感
                        onClick = onMonthClick
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp), // 胶囊内部留白
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${month.year}年${month.monthValue}月",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue // ✨ 文字使用纯主题蓝，与浅色底色呼应
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown, // 圆润的下拉箭头
                    contentDescription = "选择月份",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}