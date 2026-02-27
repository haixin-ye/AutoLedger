package com.yhx.autoledger.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.viewmodel.DataSyncViewModel
import com.yhx.autoledger.viewmodel.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataImportExportScreen(
    onBack: () -> Unit,
    // ✨ 注入负责读写与校验的 ViewModel
    viewModel: DataSyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()

    // 监听 ViewModel 中的状态变化，弹出对应的提示
    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState() // 提示完恢复空闲状态
            }
            is SyncState.Error -> {
                Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // ==========================================
    // 🚀 导出：强行指定文件后缀为 .aldata
    // ==========================================
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData(context, uri)
        }
    }

    // ==========================================
    // 🚀 导入：随便用户选，后台严格防伪校验！
    // ==========================================
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importData(context, uri)
        }
    }

    // UI 布局构建 (与之前相同，增加 Loading 状态的遮罩反馈)
    Box(modifier = Modifier.fillMaxSize().background(AppDesignSystem.colors.appBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. 顶部导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "返回", tint = AppDesignSystem.colors.textPrimary)
                }
                Text(
                    text = "数据导入与导出",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppDesignSystem.colors.textPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 导出功能卡片
            ActionCard(
                title = "导出数据到本地 (.aldata)",
                subtitle = "生成专属加密格式备份文件。妥善保管，随时随地恢复您的核心资产。",
                icon = Icons.Rounded.UploadFile,
                iconTint = AppDesignSystem.colors.brandAccent,
                onClick = {
                    // ✨ 核心：命名规则生成专属格式后缀
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                    val defaultFileName = "AutoLedger_Backup_$timestamp.aldata"
                    exportLauncher.launch(defaultFileName)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. 导入功能卡片
            ActionCard(
                title = "从专属文件恢复",
                subtitle = "仅支持解析 AutoLedger 的专属 .aldata 备份文件，双重防伪，安全可靠。",
                icon = Icons.Rounded.Download,
                iconTint = AppDesignSystem.colors.categoryTransport,
                onClick = {
                    // 允许所有文件，交给我们的魔法签名去拦截！
                    importLauncher.launch(arrayOf("*/*"))
                }
            )
        }

        // 如果正在读写中，显示一个半透明的高级防误触 Loading 遮罩
        if (syncState is SyncState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppDesignSystem.colors.brandAccent)
            }
        }
    }
}

// ================== 组件化：操作卡片 ==================
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesignSystem.dimens.spacingLarge)
            .bounceClick() // 你的高级回弹动画
            .clickable { onClick() },
        shape = RoundedCornerShape(AppDesignSystem.dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = AppDesignSystem.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDesignSystem.dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标区块
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 右侧文本区块
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppDesignSystem.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = AppDesignSystem.colors.textSecondary
                )
            }
        }
    }
}