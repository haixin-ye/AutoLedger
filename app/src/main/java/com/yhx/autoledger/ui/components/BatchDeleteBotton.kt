package com.yhx.autoledger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题

@Composable
fun BatchDeleteBotton(
    isVisible: Boolean,
    isAllSelected: Boolean,
    selectedCount: Int,
    onSelectAllToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            // ✨ 映射专属悬浮条背景色
            color = AppTheme.colors.batchActionBarBg,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：全选/取消全选区
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSelectAllToggle
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DoneAll,
                        contentDescription = if (isAllSelected) "取消全选" else "全选",
                        // ✨ 映射专属活动色和内容色
                        tint = if (isAllSelected) AppTheme.colors.batchActionBarActive else AppTheme.colors.batchActionBarContent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isAllSelected) "取消全选" else "全选",
                        // ✨ 映射专属活动色和内容色
                        color = if (isAllSelected) AppTheme.colors.batchActionBarActive else AppTheme.colors.batchActionBarContent,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // 中间：柔和的分割线
                Spacer(Modifier.width(16.dp))
                // ✨ 映射专属分割线颜色
                Box(Modifier.width(1.dp).height(20.dp).background(AppTheme.colors.batchActionBarDivider))
                Spacer(Modifier.width(16.dp))

                // 右侧：红色删除区
                Button(
                    onClick = onDeleteClick,
                    // ✨ 复用全局的警示红
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.warningRed),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        // ✨ 复用强调色上的文本色 (白色)
                        tint = AppTheme.colors.textOnAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "删除 $selectedCount",
                        // ✨ 复用强调色上的文本色 (白色)
                        color = AppTheme.colors.textOnAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}