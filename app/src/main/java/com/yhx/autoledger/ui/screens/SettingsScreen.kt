package com.yhx.autoledger.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.ui.theme.LightBlueGradient

@Composable
fun SettingsScreen() {
    var isLoggedIn by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✨ 状态1：自动记账权限状态
    var isAutoRecordingEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    // ✨ 状态2：隐私锁测试状态
    var isPrivacyLockEnabled by remember { mutableStateOf(false) }

    // ✨ 核心魔法：当用户从系统设置页返回 App 时，自动重新检查权限，刷新开关状态！
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAutoRecordingEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. 顶部标题
        item {
            Text(
                text = "我的",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
            )
        }

        // 2. 个人信息卡片
        item {
            ProfileCard(
                isLoggedIn = isLoggedIn,
                onClick = { isLoggedIn = !isLoggedIn }
            )
        }

        // 3. AI 与自动化设置组
        item {
            SettingsGroup(title = "AI 与自动化") {
                SettingSwitchRow(
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = AccentBlue,
                    title = "无障碍视觉引擎 (防漏记)",
                    subtitle = if (isAutoRecordingEnabled) "透视眼已开启，在微信支付界面自动摘取数据" else "点击去系统开启无障碍服务",
                    checked = isAutoRecordingEnabled,
                    onCheckedChange = {
                        // ✨ 核心跳转逻辑：跳往无障碍设置页
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                        if (!isAutoRecordingEnabled) {
                            Toast.makeText(context, "请在已下载的应用中找到 AutoLedger 并开启", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                Divider(Modifier.padding(start = 56.dp), color = Color(0xFFF1F2F6))
                SettingClickRow(
                    icon = Icons.Rounded.RecordVoiceOver,
                    iconTint = Color(0xFF74EBD5),
                    title = "AI 助手人设与语气",
                    value = "专业管家"
                )
            }
        }

        // 4. 数据与隐私设置组
        item {
            SettingsGroup(title = "数据与安全") {
                SettingClickRow(
                    icon = Icons.Rounded.CloudUpload,
                    iconTint = Color(0xFF9FACE6),
                    title = "数据云端同步",
                    value = "刚刚同步"
                )
                Divider(Modifier.padding(start = 56.dp), color = Color(0xFFF1F2F6))
                SettingClickRow(
                    icon = Icons.Rounded.ImportExport,
                    iconTint = Color(0xFFFAB1A0),
                    title = "导出账单数据",
                    value = "CSV / Excel"
                )
                Divider(Modifier.padding(start = 56.dp), color = Color(0xFFF1F2F6))
                SettingSwitchRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = Color(0xFFFF7675),
                    title = "隐私锁",
                    subtitle = "进入 App 需要面容或指纹验证",
                    checked = isPrivacyLockEnabled,
                    onCheckedChange = { isPrivacyLockEnabled = it }
                )
            }
        }

        // 5. 常规个性化设置组
        item {
            SettingsGroup(title = "通用设置") {
                SettingClickRow(
                    icon = Icons.Rounded.Palette,
                    iconTint = Color(0xFF81ECEC),
                    title = "主题外观",
                    value = "跟随系统"
                )
                Divider(Modifier.padding(start = 56.dp), color = Color(0xFFF1F2F6))
                SettingClickRow(
                    icon = Icons.Rounded.NotificationsActive,
                    iconTint = Color(0xFFFFB8B8),
                    title = "记账提醒",
                    value = "每天 20:00"
                )
            }
        }

        // 6. 退出登录按钮
        if (isLoggedIn) {
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { isLoggedIn = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Text("退出登录", color = Color.Red.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileCard(isLoggedIn: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(110.dp)
            .bounceClick()
            .clickable { onClick()},
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val backgroundModifier = if (isLoggedIn) Modifier.background(LightBlueGradient) else Modifier.background(Color.White)

        Row(
            modifier = Modifier.fillMaxSize().then(backgroundModifier).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = if (isLoggedIn) Color.White.copy(alpha = 0.3f) else Color(0xFFF1F2F6)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        tint = if (isLoggedIn) Color.White else Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoggedIn) "Hi, 探索者" else "点击登录 / 注册",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isLoggedIn) Color.Black else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLoggedIn) "AutoLedger Pro 已激活" else "登录后开启云端智能记账",
                    fontSize = 12.sp,
                    color = if (isLoggedIn) AccentBlue else Color.Gray,
                    fontWeight = if (isLoggedIn) FontWeight.Bold else FontWeight.Normal
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isLoggedIn) Color.Black else Color.LightGray)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingClickRow(icon: ImageVector, iconTint: Color, title: String, value: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .bounceClick()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}

// ✨ 修改点：将内部管理的 checked 提升到了函数参数，由外部传入状态
@Composable
fun SettingSwitchRow(icon: ImageVector, iconTint: Color, title: String, subtitle: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle.isEmpty()) 60.dp else 72.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = if (checked) AccentBlue else Color.Gray)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange, // ✨ 使用外部传进来的控制逻辑
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentBlue)
        )
    }
}

// 检查是否拥有无障碍权限
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/com.yhx.autoledger.autobookkeeping.AutoLedgerAccessibilityService"
    val enabledServicesSetting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    if (enabledServicesSetting == null) return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }
    return false
}