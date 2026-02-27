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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme
import com.yhx.autoledger.viewmodel.DailyRecord
import com.yhx.autoledger.viewmodel.MonthlyRecord
import java.time.YearMonth
import java.util.Calendar
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.style.TextOverflow
// ✨ 日历显示支出和收入
// ✨ 增加 onDayClick 回调
@Composable
fun CalendarGrid(month: YearMonth, dailyMap: Map<Int, DailyRecord>, onDayClick: (Int) -> Unit = {}) {
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textSecondary)
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
                CalendarDayCell(day = day, record = null, isToday = false, isCurrentMonth = false, onDayClick = onDayClick)
            }

            // 2. 填充本月日期
            val today = Calendar.getInstance()
            val isThisMonth = month.year == today.get(Calendar.YEAR) && month.monthValue == (today.get(Calendar.MONTH) + 1)

            items(daysInMonth) { index ->
                val day = index + 1
                val isToday = isThisMonth && day == today.get(Calendar.DAY_OF_MONTH)
                CalendarDayCell(
                    day = day,
                    record = dailyMap[day],
                    isToday = isToday,
                    isCurrentMonth = true,
                    onDayClick = onDayClick // ✨ 传入回调
                )
            }

            // 3. 填充下个月的起始日期
            val remainingCells = 42 - (firstDayOfWeek + daysInMonth)
            items(remainingCells) { index ->
                val day = index + 1
                CalendarDayCell(day = day, record = null, isToday = false, isCurrentMonth = false, onDayClick = onDayClick)
            }
        }
    }
}

// ✨ 增加 onDayClick 参数并实现点击
@Composable
fun CalendarDayCell(
    day: Int,
    record: DailyRecord?,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    onDayClick: (Int) -> Unit // ✨ 新增参数
) {
    val textColor = if (isCurrentMonth) {
        if (isToday) AppTheme.colors.calendarTodayText else AppTheme.colors.textPrimary
    } else {
        AppTheme.colors.textSecondary.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp)) // ✨ 切圆角，防止点击水波纹溢出方格
            .background(
                if (isToday) AppTheme.colors.calendarTodayBackground else Color.Transparent,
            )
            .clickable(
                enabled = isCurrentMonth, // ✨ 只有当前月份的日子允许被点击
                onClick = { onDayClick(day) }
            )
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (isCurrentMonth && record != null && (record.expense > 0 || record.income > 0)) {
            val netAmount = record.income - record.expense
            Text(
                text = "${if (netAmount >= 0) "+" else "-"}${Math.abs(netAmount).toInt()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = (if (netAmount >= 0) AppTheme.colors.incomeColor else AppTheme.colors.expenseColor).copy(alpha = 0.7f),
                maxLines = 1
            )
        } else {
            Text(
                text = "0",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Transparent,
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "账单明细",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = AppTheme.colors.textPrimary,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppTheme.colors.brandAccent.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMonthClick
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${month.year}年${month.monthValue}月",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.brandAccent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "选择月份",
                    tint = AppTheme.colors.brandAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}





