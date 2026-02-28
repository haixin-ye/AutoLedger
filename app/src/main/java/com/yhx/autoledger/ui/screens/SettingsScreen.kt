package com.yhx.autoledger.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.components.*
import com.yhx.autoledger.ui.theme.AppDesignSystem

@Composable
fun SettingsScreen(
    currentTheme: Int,
    currentBookName: String,
    onThemeChange: (Int) -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToCategoryManage: () -> Unit,
    onNavigateToBookManage: () -> Unit // ✨ 新增：前往账本管理的导航回调
) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(true) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var isAutoRecordingEnabled by remember { mutableStateOf(false) }
    var isPrivacyLockEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDesignSystem.colors.appBackground),
        contentPadding = PaddingValues(bottom = AppDesignSystem.dimens.spacingLarge * 2)
    ) {
        item {
            Text(
                text = "我的",
                fontSize = 28.sp,
                color = AppDesignSystem.colors.textPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(
                    start = AppDesignSystem.dimens.spacingLarge,
                    top = AppDesignSystem.dimens.spacingLarge,
                    bottom = AppDesignSystem.dimens.spacingNormal
                )
            )
        }

        item {
            ProfileCard(
                isLoggedIn = isLoggedIn,
                onClick = { isLoggedIn = !isLoggedIn }
            )
        }

        item {
            SettingsGroup(title = "账务与分类") {
                // ✨ 核心修改：接入前往账本管理的事件
                SettingClickRow(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    iconTint = AppDesignSystem.colors.brandAccent,
                    title = "多账本管理",
                    value = currentBookName,
                    onClick = onNavigateToBookManage
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.Category,
                    iconTint = AppDesignSystem.colors.categoryFood,
                    title = "收支分类管理",
                    value = "",
                    onClick = onNavigateToCategoryManage
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.CreditCard,
                    iconTint = AppDesignSystem.colors.categoryTransport,
                    title = "支付渠道配置",
                    value = "微信 / 支付宝",
                    onClick = { /* TODO */ }
                )
            }
        }

        item {
            SettingsGroup(title = "AI 与自动化") {
                SettingSwitchRow(
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = AppDesignSystem.colors.iconBgAI,
                    title = "无障碍自动记账",
                    subtitle = if (isAutoRecordingEnabled) "正在后台捕捉支付数据" else "需开启系统无障碍权限",
                    initialChecked = isAutoRecordingEnabled,
                    onCheckedChange = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        isAutoRecordingEnabled = it
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.Face,
                    iconTint = AppDesignSystem.colors.brandAccent,
                    title = "AI 助手人设与语气",
                    value = "专业管家",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.Memory,
                    iconTint = AppDesignSystem.colors.categoryShop,
                    title = "AI 专属记忆管理",
                    value = "优化学习模型",
                    onClick = { /* TODO */ }
                )
            }
        }

        item {
            SettingsGroup(title = "数据与安全") {
                SettingClickRow(
                    icon = Icons.Rounded.CloudUpload,
                    iconTint = AppDesignSystem.colors.iconBgCloud,
                    title = "数据云端同步",
                    value = "刚刚同步",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.ImportExport,
                    iconTint = AppDesignSystem.colors.iconBgExport,
                    title = "导入/导出数据",
                    value = "aldata",
                    onClick = onNavigateToImportExport
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingSwitchRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = AppDesignSystem.colors.iconBgSecurity,
                    title = "隐私锁防窥视",
                    subtitle = "进入 App 需要面容或指纹验证",
                    initialChecked = isPrivacyLockEnabled,
                    onCheckedChange = { isPrivacyLockEnabled = it }
                )
            }
        }

        item {
            SettingsGroup(title = "通用设置") {
                ThemeSelectionRow(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.NotificationsActive,
                    iconTint = AppDesignSystem.colors.iconBgAlert,
                    title = "记账提醒",
                    value = "每天 20:00",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = AppDesignSystem.colors.dividerColor)
                SettingClickRow(
                    icon = Icons.Rounded.Info,
                    iconTint = AppDesignSystem.colors.textSecondary,
                    title = "关于 AutoLedger",
                    value = "v1.0.0",
                    onClick = { /* TODO */ }
                )
            }
        }

        if (isLoggedIn) {
            item {
                Spacer(Modifier.height(AppDesignSystem.dimens.spacingLarge))
                Button(
                    onClick = { isLoggedIn = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDesignSystem.dimens.spacingLarge)
                        .height(AppDesignSystem.dimens.buttonHeight)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppDesignSystem.colors.cardBackground),
                    shape = RoundedCornerShape(AppDesignSystem.dimens.spacingNormal),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = AppDesignSystem.dimens.cardElevation)
                ) {
                    Text(
                        text = "退出登录",
                        color = AppDesignSystem.colors.logoutButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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