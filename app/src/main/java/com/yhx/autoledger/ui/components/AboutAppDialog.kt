package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppDesignSystem

@Composable
fun AboutAppDialog(
    version: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppDesignSystem.colors.appBackground,
        title = null, // 我们直接在 text 区域自定义完整的顶部UI
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 应用 Logo 占位
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

                // 2. 应用名称
                Text(
                    text = "AutoLedger",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AppDesignSystem.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. 版本号
                Text(
                    text = version,
                    fontSize = 14.sp,
                    color = AppDesignSystem.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 应用介绍 Slogan
                Text(
                    text = "一款基于 AI 驱动的智能记账应用。\n让财务管理变得前所未有的简单与贴心。",
                    fontSize = 14.sp,
                    color = AppDesignSystem.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // TODO: 后续可以在这里添加 Row，放置“用户协议”和“隐私政策”等可点击的文字按钮

                // 5. 版权与开发者信息
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