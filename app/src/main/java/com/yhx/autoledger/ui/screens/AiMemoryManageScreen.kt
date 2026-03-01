package com.yhx.autoledger.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.viewmodel.AiMemoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMemoryManageScreen(
    onBack: () -> Unit,
    viewModel: AiMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val savedInstructions by viewModel.customInstructions.collectAsState()

    // 本地状态，用于输入框的双向绑定
    var inputText by remember(savedInstructions) { mutableStateOf(savedInstructions) }

    Scaffold(
        containerColor = AppDesignSystem.colors.appBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "配置专属记账规则",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppDesignSystem.colors.appBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // 模块一：规则编辑区
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "告诉 AI 您的消费习惯",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppDesignSystem.colors.textPrimary
                )
                Text(
                    "用大白话输入即可。保存后，AI 每次记账都会严格遵守这些规则。",
                    fontSize = 13.sp,
                    color = AppDesignSystem.colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    placeholder = {
                        Text(
                            text = "例如：\n1. 凡是提到'星巴克'、'瑞幸'都归类为'餐饮'。\n2. 我在杭州工作，提到'打车'默认是交通。\n",
                            fontSize = 13.sp, // 字体调小
                            color = AppDesignSystem.colors.textTertiary
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppDesignSystem.colors.brandAccent,
                        unfocusedBorderColor = AppDesignSystem.colors.dividerColor
                    )
                )

                Button(
                    onClick = {
                        viewModel.saveInstructions(inputText)
                        focusManager.clearFocus()
                        Toast.makeText(context, "记忆规则已保存生效！", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(50.dp)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppDesignSystem.colors.brandAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("保存规则", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = AppDesignSystem.colors.dividerColor)

            // ==========================================
            // 模块二：一键恢复默认状态（保留您喜欢的卡片UI）
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "恢复默认状态",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppDesignSystem.colors.warningRed
                )
                Text(
                    "如果您想让 AI 恢复初始的记账习惯，可以一键清空上方的所有自定义规则。",
                    fontSize = 13.sp,
                    color = AppDesignSystem.colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppDesignSystem.colors.cardBackground)
                        .bounceClick()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.AutoDelete,
                        contentDescription = null,
                        tint = AppDesignSystem.colors.warningRed
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "清空全部规则",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDesignSystem.colors.textPrimary
                        )
                    }
                    Button(
                        onClick = {
                            // ✨ 核心修改：点击清空时，同步清空 UI 状态和底层数据
                            inputText = ""
                            viewModel.saveInstructions("")
                            focusManager.clearFocus()
                            Toast.makeText(
                                context,
                                "专属规则已清空，AI 已恢复默认状态",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppDesignSystem.colors.warningRed.copy(
                                alpha = 0.1f
                            ), contentColor = AppDesignSystem.colors.warningRed
                        ),
                        elevation = null
                    ) {
                        Text("清空")
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}