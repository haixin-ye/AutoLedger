package com.yhx.autoledger.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem

@Composable
fun CategoryIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current

    // 尝试识别是否为 Android 资源 ID (兼容旧版 ic_food 等)
    val resId = remember(iconName) {
        context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    if (resId != 0) {
        // 如果是资源图片，则使用 Icon 渲染
        Icon(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = modifier,
            tint = if (tint == Color.Unspecified) AppDesignSystem.colors.textPrimary else tint
        )
    } else {
        // 如果是自定义 Emoji，则使用 Text 渲染
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = iconName,
                fontSize = 22.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(emojiSupportMatch = EmojiSupportMatch.Default)
                )
            )
        }
    }
}