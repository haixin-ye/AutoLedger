package com.yhx.autoledger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.navigation.bottomNavItems
import com.yhx.autoledger.ui.theme.AppTheme

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = AppTheme.colors.bottomBarBackground.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        // ✨ 修改点 1：移除这里的 navigationBarsPadding()
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            // ✨ 修改点 2：将 padding 加到内部的 Row 这里，并且放在 height() 之前
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // 让内部内容被小白条顶起
                .height(80.dp),          // 保持内容区域自身的高度为 80.dp
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route

                val contentColor by animateColorAsState(
                    targetValue = if (selected) AppTheme.colors.brandAccent else AppTheme.colors.textTertiary,
                    animationSpec = tween(durationMillis = 300),
                    label = "colorAnimation"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(screen.route) }
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = contentColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = screen.title,
                        fontSize = 12.sp,
                        color = contentColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}