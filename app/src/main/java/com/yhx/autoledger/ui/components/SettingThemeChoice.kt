package com.yhx.autoledger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme

@Composable
fun ThemeSelectionRow(
    currentTheme: Int,
    onThemeChange: (Int) -> Unit
) {
    val options = listOf("跟随系统", "浅色", "深色")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.dimens.listItemHeight)
            .padding(horizontal = AppTheme.dimens.spacingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标与标题
        Box(
            modifier = Modifier
                .size(AppTheme.dimens.iconBgSize)
                .background(AppTheme.colors.iconBgTheme.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Palette,
                contentDescription = null,
                tint = AppTheme.colors.iconBgTheme,
                modifier = Modifier.size(AppTheme.dimens.iconSizeNormal)
            )
        }
        Spacer(modifier = Modifier.width(AppTheme.dimens.spacingNormal))
        Text(
            "主题外观",
            color = AppTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // ✨ 核心改造：右侧分段选择器
        Row(
            modifier = Modifier
                .background(AppTheme.colors.appBackground, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            options.forEachIndexed { index, text ->
                val isSelected = currentTheme == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) AppTheme.colors.brandAccent else Color.Transparent,
                    label = "bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                    label = "text"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable { onThemeChange(index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}