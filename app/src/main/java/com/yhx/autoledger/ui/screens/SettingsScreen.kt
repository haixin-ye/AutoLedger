package com.yhx.autoledger.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign // ✨ 确保引入了 TextAlign
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
    privacyLockPattern: String,
    reminderTime: String,
    aiPersonaId: String,
    allPersonas: List<com.yhx.autoledger.model.AIPersona>,
    onThemeChange: (Int) -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToCategoryManage: () -> Unit,
    onNavigateToBookManage: () -> Unit,
    onNavigateToAiMemory: () -> Unit,
    onTogglePrivacyLock: (Boolean) -> Unit,
    onSetPrivacyPattern: (String) -> Unit,
    onSetReminderTime: (String) -> Unit,
    onSetAiPersonaId: (String) -> Unit,
) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(true) }

    // 统一管理所有的弹窗状态
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPersonaDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPatternSetup by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) showTimePicker = true
            else Toast.makeText(context, "需开启通知权限才能准时提醒您哦", Toast.LENGTH_SHORT).show()
        }

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
                // 支付渠道配置已暂时关闭
            }
        }

        item {
            SettingsGroup(title = "AI 与自动化") {
                // 无障碍自动记账已暂时关闭

                // 匹配当前的人设对象
                val currentPersona = allPersonas.find { it.id == aiPersonaId } ?: allPersonas.firstOrNull()

                SettingClickRow(
                    icon = Icons.Rounded.Face,
                    iconTint = AppDesignSystem.colors.brandAccent,
                    title = "AI 助手人设与语气",
                    value = if (currentPersona != null) "${currentPersona.avatar} ${currentPersona.name}" else "读取中...",
                    onClick = { showPersonaDialog = true }
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
                    onClick = onNavigateToAiMemory
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
                    title = "隐私锁",
                    subtitle = if (privacyLockEnabled) "已开启" else "应用退到后台时保护数据",
                    initialChecked = privacyLockEnabled,
                    onCheckedChange = { isChecking ->
                        if (isChecking) {
                            showPatternSetup = true
                        } else {
                            onTogglePrivacyLock(false)
                            onSetPrivacyPattern("")
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
                    value = if (reminderTime.isEmpty()) "未开启" else "每天 $reminderTime",
                    onClick = {
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
                    onClick = { showAboutDialog = true }
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

    // ==============================================
    // 弹窗组件调用区域
    // ==============================================

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

    if (showPersonaDialog) {
        AIPersonaSelectionDialog(
            allPersonas = allPersonas,
            currentPersonaId = aiPersonaId,
            onDismiss = { showPersonaDialog = false },
            onPersonaSelect = { selectedId ->
                onSetAiPersonaId(selectedId)
                showPersonaDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            version = "v1.0.0",
            onDismiss = { showAboutDialog = false }
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
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(50.dp).clip(RoundedCornerShape(12.dp)).background(AppDesignSystem.colors.brandAccent.copy(alpha = 0.1f)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        InfiniteWheelPicker(items = (0..23).toList(), initialItem = selectedHour, onItemSelected = { selectedHour = it })
                        Text(":", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.brandAccent, modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-2).dp))
                        InfiniteWheelPicker(items = (0..59).toList(), initialItem = selectedMinute, onItemSelected = { selectedMinute = it })
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
                TextButton(onClick = { onSetReminderTime(""); showTimePicker = false }) { Text("关闭提醒", color = AppDesignSystem.colors.warningRed) }
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
                    Text(text = if (setupError) "至少需要连接 4 个点" else "请绘制您的解锁图案", color = if (setupError) AppDesignSystem.colors.warningRed else AppDesignSystem.colors.textSecondary, modifier = Modifier.padding(bottom = 16.dp))
                    PatternLock(
                        isError = setupError,
                        onPatternComplete = { patternList ->
                            if (patternList.size < 4) setupError = true
                            else {
                                onSetPrivacyPattern(patternList.joinToString(","))
                                onTogglePrivacyLock(true)
                                showPatternSetup = false
                                Toast.makeText(context, "密码设置成功！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showPatternSetup = false }) { Text("取消", color = AppDesignSystem.colors.textSecondary) } }
        )
    }
}

// ==============================================
// 独立抽取的弹窗组件
// ==============================================

@Composable
fun AIPersonaSelectionDialog(
    allPersonas: List<com.yhx.autoledger.model.AIPersona>,
    currentPersonaId: String,
    onDismiss: () -> Unit,
    onPersonaSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppDesignSystem.colors.appBackground,
        title = {
            Text(
                text = "选择专属 AI 助手",
                fontWeight = FontWeight.Bold,
                color = AppDesignSystem.colors.textPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allPersonas) { persona ->
                    val isSelected = persona.id == currentPersonaId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) AppDesignSystem.colors.brandAccent.copy(alpha = 0.1f)
                                else AppDesignSystem.colors.cardBackground
                            )
                            .clickable {
                                onPersonaSelect(persona.id)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(persona.avatar, fontSize = 32.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AppDesignSystem.colors.brandAccent else AppDesignSystem.colors.textPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = persona.description,
                                fontSize = 12.sp,
                                color = AppDesignSystem.colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = AppDesignSystem.colors.brandAccent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = AppDesignSystem.colors.textSecondary)
            }
        }
    )
}

// ✨ 这里就是补充的 AboutAppDialog 弹窗组件定义
@Composable
fun AboutAppDialog(
    version: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppDesignSystem.colors.appBackground,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppDesignSystem.colors.brandAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(40.dp),
                        tint = AppDesignSystem.colors.brandAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AutoLedger",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AppDesignSystem.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = version,
                    fontSize = 14.sp,
                    color = AppDesignSystem.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "一款基于 AI 驱动的智能记账应用。\n让财务管理变得前所未有的简单与贴心。",
                    fontSize = 14.sp,
                    color = AppDesignSystem.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Copyright © 2026 yhx.\nAll Rights Reserved.",
                    fontSize = 12.sp,
                    color = AppDesignSystem.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "确定",
                    color = AppDesignSystem.colors.brandAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    )
}