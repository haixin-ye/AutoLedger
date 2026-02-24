package com.yhx.autoledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush // ✨ 新增 Brush 导入
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AppTheme
// 删除了对 LightBlueGradient 的强行引用，改为动态生成

@Composable
fun SettingsScreen(
    currentTheme: Int,
    onThemeChange: (Int) -> Unit
) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.appBackground),
        contentPadding = PaddingValues(bottom = AppTheme.dimens.spacingLarge * 2)
    ) {
        item {
            Text(
                text = "我的",
                fontSize = 28.sp,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(
                    start = AppTheme.dimens.spacingLarge,
                    top = AppTheme.dimens.spacingLarge,
                    bottom = AppTheme.dimens.spacingNormal
                )
            )
        }

        item {
            ProfileCard(isLoggedIn = isLoggedIn, onClick = { isLoggedIn = !isLoggedIn })
        }

        item {
            SettingsGroup(title = "AI 与自动化") {
                SettingSwitchRow(
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = AppTheme.colors.brandAccent,
                    title = "后台自动记账",
                    subtitle = "允许读取通知和剪贴板智能解析",
                    initialChecked = true
                )
                Divider(Modifier.padding(start = 56.dp), color = AppTheme.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.RecordVoiceOver,
                    iconTint = AppTheme.colors.iconBgAI,
                    title = "AI 助手人设与语气",
                    value = "专业管家"
                )
            }
        }

        item {
            SettingsGroup(title = "数据与安全") {
                SettingClickRow(
                    icon = Icons.Rounded.CloudUpload,
                    iconTint = AppTheme.colors.iconBgCloud,
                    title = "数据云端同步",
                    value = "刚刚同步"
                )
                Divider(Modifier.padding(start = 56.dp), color = AppTheme.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.ImportExport,
                    iconTint = AppTheme.colors.iconBgExport,
                    title = "导出账单数据",
                    value = "CSV / Excel"
                )
                Divider(Modifier.padding(start = 56.dp), color = AppTheme.colors.dividerColor)
                SettingSwitchRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = AppTheme.colors.iconBgSecurity,
                    title = "隐私锁",
                    subtitle = "进入 App 需要面容或指纹验证",
                    initialChecked = false
                )
            }
        }

        item {
            SettingsGroup(title = "通用设置") {
                SettingClickRow(
                    icon = Icons.Rounded.Palette,
                    iconTint = AppTheme.colors.iconBgTheme,
                    title = "主题外观",
                    value = when(currentTheme) {
                        1 -> "浅色模式"
                        2 -> "深色模式"
                        else -> "跟随系统"
                    },
                    onClick = { showThemeDialog = true }
                )
                Divider(Modifier.padding(start = 56.dp), color = AppTheme.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.NotificationsActive,
                    iconTint = AppTheme.colors.iconBgAlert,
                    title = "记账提醒",
                    value = "每天 20:00"
                )
            }
        }

        if (isLoggedIn) {
            item {
                Spacer(Modifier.height(AppTheme.dimens.spacingLarge))
                Button(
                    onClick = { isLoggedIn = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.dimens.spacingLarge)
                        .height(AppTheme.dimens.buttonHeight)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.cardBackground),
                    shape = RoundedCornerShape(AppTheme.dimens.spacingNormal),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = AppTheme.dimens.cardElevation)
                ) {
                    // ✨ 替换退出按钮颜色
                    Text("退出登录", color = AppTheme.colors.logoutButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelect = { selectedTheme ->
                onThemeChange(selectedTheme)
                showThemeDialog = false
            }
        )
    }
}

// ============== 拆分的 UI 组件 ==============

@Composable
fun ProfileCard(isLoggedIn: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimens.spacingMedium, vertical = AppTheme.dimens.spacingSmall)
            .height(110.dp)
            .bounceClick()
            .clickable { onClick() },
        shape = RoundedCornerShape(AppTheme.dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppTheme.dimens.cardElevation)
    ) {
        // ✨ 动态组装主题的渐变色
        val dynamicGradient = Brush.horizontalGradient(
            colors = listOf(AppTheme.colors.profileGradientStart, AppTheme.colors.profileGradientEnd)
        )
        val backgroundModifier = if (isLoggedIn) Modifier.background(dynamicGradient) else Modifier.background(Color.Transparent)

        Row(
            modifier = Modifier.fillMaxSize().then(backgroundModifier).padding(AppTheme.dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                // ✨ 替换硬编码的白色 30%
                color = if (isLoggedIn) AppTheme.colors.profileAvatarBgOnGradient else AppTheme.colors.dividerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        // ✨ 替换硬编码的白色
                        tint = if (isLoggedIn) AppTheme.colors.profileAvatarIconOnGradient else AppTheme.colors.textTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppTheme.dimens.spacingNormal))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoggedIn) "Hi, 探索者" else "点击登录 / 注册",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    // ✨ 替换硬编码的黑色
                    color = if (isLoggedIn) AppTheme.colors.profileTextOnGradient else AppTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLoggedIn) "AutoLedger Pro 已激活" else "登录后开启云端智能记账",
                    fontSize = 12.sp,
                    // ✨ 替换之前强制复用的 brandAccent
                    color = if (isLoggedIn) AppTheme.colors.profileTextSubOnGradient else AppTheme.colors.textSecondary,
                    fontWeight = if (isLoggedIn) FontWeight.Bold else FontWeight.Normal
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isLoggedIn) AppTheme.colors.profileTextOnGradient else AppTheme.colors.textTertiary)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = AppTheme.dimens.spacingMedium, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.cardBackground,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingClickRow(icon: ImageVector, iconTint: Color, title: String, value: String = "", onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.dimens.listItemHeight)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = AppTheme.dimens.spacingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(AppTheme.dimens.iconBgSize).background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppTheme.dimens.iconSizeNormal))
        }
        Spacer(modifier = Modifier.width(AppTheme.dimens.spacingNormal))
        Text(title, color = AppTheme.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppTheme.colors.textTertiary, modifier = Modifier.size(AppTheme.dimens.iconSizeNormal))
    }
}

@Composable
fun SettingSwitchRow(icon: ImageVector, iconTint: Color, title: String, subtitle: String = "", initialChecked: Boolean) {
    var checked by remember { mutableStateOf(initialChecked) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle.isEmpty()) AppTheme.dimens.listItemHeight else 72.dp)
            .padding(horizontal = AppTheme.dimens.spacingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(AppTheme.dimens.iconBgSize).background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppTheme.dimens.iconSizeNormal))
        }
        Spacer(modifier = Modifier.width(AppTheme.dimens.spacingNormal))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppTheme.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = AppTheme.colors.textSecondary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            // ✨ 替换这里的 Color.White 为定制色
            colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.colors.switchThumbChecked, checkedTrackColor = AppTheme.colors.brandAccent)
        )
    }
}

@Composable
fun ThemeSelectionDialog(currentTheme: Int, onDismiss: () -> Unit, onThemeSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.cardBackground,
        title = { Text("选择主题外观", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ThemeOptionRow("跟随系统", 0, currentTheme, onThemeSelect)
                ThemeOptionRow("浅色模式", 1, currentTheme, onThemeSelect)
                ThemeOptionRow("深色模式", 2, currentTheme, onThemeSelect)
            }
        },
        confirmButton = {
            // ✨ 将这里的颜色解耦
            TextButton(onClick = onDismiss) { Text("取消", color = AppTheme.colors.dialogConfirmText) }
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
            colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.brandAccent, unselectedColor = AppTheme.colors.textSecondary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = AppTheme.colors.textPrimary)
    }
}