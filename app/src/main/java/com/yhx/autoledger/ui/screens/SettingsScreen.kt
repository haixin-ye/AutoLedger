package com.yhx.autoledger.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.components.*
import com.yhx.autoledger.ui.theme.AppDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: Int,
    currentBookName: String,
    privacyLockEnabled: Boolean,
    privacyLockPattern: String, // ✨ 新增
    reminderTime: String,
    onThemeChange: (Int) -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToCategoryManage: () -> Unit,
    onNavigateToBookManage: () -> Unit, // 前往账本管理的导航回调
    onNavigateToAiMemory: () -> Unit,
    onTogglePrivacyLock: (Boolean) -> Unit,
    onSetPrivacyPattern: (String) -> Unit, // ✨ 新增
    onSetReminderTime: (String) -> Unit
) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(true) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var isAutoRecordingEnabled by remember { mutableStateOf(false) }
    var isPrivacyLockEnabled by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    // ✨ 申请通知权限的 Launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) showTimePicker = true
            else Toast.makeText(context, "需开启通知权限才能准时提醒您哦", Toast.LENGTH_SHORT)
                .show()
        }
    // 增加一个状态控制设置密码弹窗
    var showPatternSetup by remember { mutableStateOf(false) }

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
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingClickRow(
                    icon = Icons.Rounded.Category,
                    iconTint = AppDesignSystem.colors.categoryFood,
                    title = "收支分类管理",
                    value = "",
                    onClick = onNavigateToCategoryManage
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
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
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingClickRow(
                    icon = Icons.Rounded.Face,
                    iconTint = AppDesignSystem.colors.brandAccent,
                    title = "AI 助手人设与语气",
                    value = "专业管家",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingClickRow(
                    icon = Icons.Rounded.Memory,
                    iconTint = AppDesignSystem.colors.categoryShop,
                    title = "AI 专属记忆管理",
                    value = "优化AI模型",
                    onClick = onNavigateToAiMemory // ✨ 绑定跳转
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
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingClickRow(
                    icon = Icons.Rounded.ImportExport,
                    iconTint = AppDesignSystem.colors.iconBgExport,
                    title = "导入/导出数据",
                    value = "aldata",
                    onClick = onNavigateToImportExport
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingSwitchRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = AppDesignSystem.colors.iconBgSecurity,
                    title = "九宫格隐私锁",
                    subtitle = if (privacyLockEnabled) "已开启" else "应用退到后台时保护数据",
                    initialChecked = privacyLockEnabled,
                    onCheckedChange = { isChecking ->
                        if (isChecking) {
                            // 想要开启，弹出设置密码界面
                            showPatternSetup = true
                        } else {
                            // 关闭直接关
                            onTogglePrivacyLock(false)
                            onSetPrivacyPattern("") // 清空密码
                        }
                    }
                )
            }
        }

        item {
            SettingsGroup(title = "通用设置") {
                ThemeSelectionRow(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
                SettingClickRow(
                    icon = Icons.Rounded.NotificationsActive,
                    iconTint = AppDesignSystem.colors.iconBgAlert,
                    title = "记账提醒",
                    value = if (reminderTime.isEmpty()) "未开启" else "每天 $reminderTime", // ✨ 动态文案
                    onClick = {
                        // Android 13 及以上需动态申请通知权限
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showTimePicker = true
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AppDesignSystem.colors.dividerColor
                )
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

    if (showTimePicker) {
        var selectedHour by remember { mutableStateOf(if (reminderTime.isNotEmpty()) reminderTime.split(":")[0].toInt() else 20) }
        var selectedMinute by remember { mutableStateOf(if (reminderTime.isNotEmpty()) reminderTime.split(":")[1].toInt() else 0) }

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = AppDesignSystem.colors.sheetBackground,
            title = { Text("设置提醒时间", fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textPrimary) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // ✨ 这是精髓：绘制一条横跨“时”与“分”的统一高亮底条 (只用品牌色的 10% 透明度)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // 宽度占 80%
                            .height(50.dp) // 和 itemHeight 保持绝对一致
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(AppDesignSystem.colors.brandAccent.copy(alpha = 0.1f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfiniteWheelPicker(
                            items = (0..23).toList(),
                            initialItem = selectedHour,
                            onItemSelected = { selectedHour = it }
                        )

                        Text(
                            text = ":",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDesignSystem.colors.brandAccent, // 冒号也使用主题强调色
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .offset(y = (-2).dp) // 微调冒号的视觉中心对齐
                        )

                        InfiniteWheelPicker(
                            items = (0..59).toList(),
                            initialItem = selectedMinute,
                            onItemSelected = { selectedMinute = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val timeStr = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onSetReminderTime(timeStr)
                    showTimePicker = false
                    Toast.makeText(context, "已设置每天 $timeStr 提醒记账", Toast.LENGTH_SHORT).show()
                }) { Text("保存", color = AppDesignSystem.colors.brandAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onSetReminderTime("")
                    showTimePicker = false
                }) { Text("关闭提醒", color = AppDesignSystem.colors.warningRed) }
            }
        )
    }

    if (showPatternSetup) {
        var setupError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPatternSetup = false },
            containerColor = AppDesignSystem.colors.appBackground,
            title = { Text("设置解锁图案", fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textPrimary) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (setupError) "至少需要连接 4 个点" else "请绘制您的解锁图案",
                        color = if (setupError) AppDesignSystem.colors.warningRed else AppDesignSystem.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PatternLock(
                        isError = setupError,
                        onPatternComplete = { patternList ->
                            if (patternList.size < 4) {
                                setupError = true
                            } else {
                                // 绘制成功！保存并开启
                                val patternStr = patternList.joinToString(",")
                                onSetPrivacyPattern(patternStr)
                                onTogglePrivacyLock(true)
                                showPatternSetup = false
                                Toast.makeText(context, "密码设置成功！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPatternSetup = false }) {
                    Text("取消", color = AppDesignSystem.colors.textSecondary)
                }
            }
        )
    }
}