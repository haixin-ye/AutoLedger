package com.yhx.autoledger.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
    allPersonas: List<com.yhx.autoledger.model.AIPersona>, // 注意：检查你的包名是 model 还是 models
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

    // ✨ 所有弹窗的状态控制变量统一放在这里（最顶层）
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPersonaDialog by remember { mutableStateOf(false) } // 控制人设弹窗
    var showTimePicker by remember { mutableStateOf(false) }
    var showPatternSetup by remember { mutableStateOf(false) }

    var isAutoRecordingEnabled by remember { mutableStateOf(false) }

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

                // ✨ 修复点 1：删除了这里原本多余的 var showPersonaDialog 声明，直接使用外部的！
                // 匹配当前的人设对象 (安全调用 firstOrNull 防止列表为空崩溃)
                val currentPersona = allPersonas.find { it.id == aiPersonaId } ?: allPersonas.firstOrNull()

                SettingClickRow(
                    icon = Icons.Rounded.Face,
                    iconTint = AppDesignSystem.colors.brandAccent,
                    title = "AI 助手人设与语气",
                    value = if (currentPersona != null) "${currentPersona.avatar} ${currentPersona.name}" else "读取中...",
                    onClick = { showPersonaDialog = true } // 点击触发最外层的变量
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

    // ==============================================
    // 以下是各种弹窗组件的调用
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

    // ✨ 修复点 2：将 AI 人设切换独立抽取为组件调用，逻辑更清晰
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

    // ... （此处保留原有 showTimePicker 和 showPatternSetup 的实现，为节省空间暂不修改它们，只保持原样）
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

// ✨ 修复点 3：抽取的独立弹窗组件
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
                            // ✨ 修复点 4：移除了 bounceClick()，直接使用 clickable，确保点击事件不被吞掉！
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