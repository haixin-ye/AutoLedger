package com.yhx.autoledger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem

// 1. 高级用户信息卡片
@Composable
fun ProfileCard(isLoggedIn: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesignSystem.dimens.spacingMedium, vertical = AppDesignSystem.dimens.spacingSmall)
            .height(110.dp)
            .bounceClick()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(AppDesignSystem.dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AppDesignSystem.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDesignSystem.dimens.cardElevation)
    ) {
        // 动态组装主题的渐变色
        val dynamicGradient = Brush.horizontalGradient(
            colors = listOf(AppDesignSystem.colors.profileGradientStart, AppDesignSystem.colors.profileGradientEnd)
        )
        val backgroundModifier = if (isLoggedIn) Modifier.background(dynamicGradient) else Modifier.background(Color.Transparent)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(AppDesignSystem.dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = if (isLoggedIn) AppDesignSystem.colors.profileAvatarBgOnGradient else AppDesignSystem.colors.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        tint = if (isLoggedIn) AppDesignSystem.colors.profileAvatarIconOnGradient else AppDesignSystem.colors.textTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppDesignSystem.dimens.spacingNormal))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoggedIn) "Hi, 探索者" else "点击登录 / 注册",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isLoggedIn) AppDesignSystem.colors.profileTextOnGradient else AppDesignSystem.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLoggedIn) "AutoLedger Pro 已激活" else "登录后开启云端智能记账",
                    fontSize = 12.sp,
                    color = if (isLoggedIn) AppDesignSystem.colors.profileTextSubOnGradient else AppDesignSystem.colors.textSecondary,
                    fontWeight = if (isLoggedIn) FontWeight.Bold else FontWeight.Normal
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isLoggedIn) AppDesignSystem.colors.profileTextOnGradient else AppDesignSystem.colors.textTertiary
            )
        }
    }
}

// 2. 设置分组外框组件
@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = AppDesignSystem.dimens.spacingMedium, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AppDesignSystem.colors.textSecondary,
            modifier = Modifier.padding(start = 12.dp, bottom = AppDesignSystem.dimens.spacingSmall)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppDesignSystem.colors.cardBackground,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

// 3. 通用点击行组件
@Composable
fun SettingClickRow(icon: ImageVector, iconTint: Color, title: String, value: String = "", onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesignSystem.dimens.listItemHeight)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick?.invoke() }
            .padding(horizontal = AppDesignSystem.dimens.spacingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(AppDesignSystem.dimens.iconBgSize)
                .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppDesignSystem.dimens.iconSizeNormal))
        }
        Spacer(modifier = Modifier.width(AppDesignSystem.dimens.spacingNormal))
        Text(title, color = AppDesignSystem.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = AppDesignSystem.colors.textSecondary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppDesignSystem.colors.textTertiary, modifier = Modifier.size(AppDesignSystem.dimens.iconSizeNormal))
    }
}

// 4. 通用 Switch 行组件 (带高动态色彩响应)
@Composable
fun SettingSwitchRow(icon: ImageVector, iconTint: Color, title: String, subtitle: String = "", initialChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // 动态色彩响应：当 Switch 开启时，图标背景色轻微加深，增强状态感知
    val animatedIconBg by animateColorAsState(
        targetValue = if (initialChecked) iconTint.copy(alpha = 0.25f) else iconTint.copy(alpha = 0.10f),
        animationSpec = tween(300), label = "iconBgAnim"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppDesignSystem.dimens.listItemHeight)
            .padding(horizontal = AppDesignSystem.dimens.spacingNormal, vertical = AppDesignSystem.dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(AppDesignSystem.dimens.iconBgSize)
                .background(animatedIconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppDesignSystem.dimens.iconSizeNormal))
        }
        Spacer(modifier = Modifier.width(AppDesignSystem.dimens.spacingNormal))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppDesignSystem.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 11.sp, color = AppDesignSystem.colors.textSecondary)
            }
        }
        Switch(
            checked = initialChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppDesignSystem.colors.switchThumbChecked,
                checkedTrackColor = AppDesignSystem.colors.brandAccent,
                uncheckedTrackColor = AppDesignSystem.colors.surfaceVariant,
                uncheckedThumbColor = AppDesignSystem.colors.textSecondary
            )
        )
    }
}

// 5. 主题选择弹窗相关组件
@Composable
fun ThemeSelectionDialog(currentTheme: Int, onDismiss: () -> Unit, onThemeSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppDesignSystem.colors.cardBackground,
        title = { Text("选择主题外观", color = AppDesignSystem.colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ThemeOptionRow("跟随系统", 0, currentTheme, onThemeSelect)
                ThemeOptionRow("浅色模式", 1, currentTheme, onThemeSelect)
                ThemeOptionRow("深色模式", 2, currentTheme, onThemeSelect)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = AppDesignSystem.colors.dialogConfirmText) }
        }
    )
}

@Composable
fun ThemeOptionRow(text: String, value: Int, currentValue: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = value == currentValue,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = AppDesignSystem.colors.brandAccent,
                unselectedColor = AppDesignSystem.colors.textSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = AppDesignSystem.colors.textPrimary)
    }
}