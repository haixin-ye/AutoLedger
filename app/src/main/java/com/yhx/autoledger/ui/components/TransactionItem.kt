package com.yhx.autoledger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.getDefault

data class TransactionData(
    val title: String,
    val icon: String,
    val amount: String,
    val color: Color,
    val originalLedger: LedgerEntity? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RefinedTransactionItem(
    data: TransactionData,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelectionMode) 6.dp else 1.dp,
        label = "floating_elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jiggle_transition")
    val randomOffset = remember { (0..120).random() }

    val rotation by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(randomOffset)
        ),
        label = "jiggle_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                if (isSelectionMode) {
                    rotationZ = rotation
                    scaleX = 1.01f
                    scaleY = 1.01f
                }
            }
            .bounceClick(),
        // ✨ 复用全局卡片底色
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedVisibility(
                visible = isSelectionMode,
                enter = expandHorizontally(
                    animationSpec = tween(250),
                    expandFrom = Alignment.Start
                ) + fadeIn(tween(250)),
                exit = shrinkHorizontally(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        // ✨ 选中态背景色复用全局品牌色
                        .background(if (isSelected) AppTheme.colors.brandAccent else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            // ✨ 边框颜色复用：选中为品牌色，未选中为全局第三级弱灰色
                            color = if (isSelected) AppTheme.colors.brandAccent else AppTheme.colors.textTertiary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            // ✨ 对勾图标复用强调色上的白字配置
                            tint = AppTheme.colors.textOnAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 原始图标区域 (这里的 data.color 是从分类数据中动态获取的主题色，保持不变)
            Box(
                Modifier
                    .size(48.dp)
                    .background(data.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(data.icon, fontSize = 22.sp)
            }

            Spacer(Modifier.width(16.dp))

            // 文本信息区域
            Column(Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    // ✨ 映射全局主标题色
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val timeString = data.originalLedger?.let {
                    SimpleDateFormat("HH:mm", getDefault()).format(Date(it.timestamp))
                } ?: "12:00"

                // ✨ 映射全局副标题色
                Text(timeString, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
            }

            Spacer(Modifier.width(8.dp))

            // 金额区域
            // ✨ 完美映射全局的收入绿和支出红
            val amountColor = if (data.amount.contains("+")) AppTheme.colors.incomeColor else AppTheme.colors.expenseColor
            Text(
                text = data.amount,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}