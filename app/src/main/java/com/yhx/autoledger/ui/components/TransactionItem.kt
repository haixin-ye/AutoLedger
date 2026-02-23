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
import com.yhx.autoledger.ui.theme.AccentBlue
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
    // ✨ 魔法 1：动态悬浮阴影 (Z轴高度变化)
    // 正常状态是 1.dp 贴近背景，进入多选模式后瞬间抬高到 6.dp，产生“浮起来”的视觉感受
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelectionMode) 6.dp else 1.dp,
        label = "floating_elevation"
    )

    // ✨ 魔法 2：iOS 风格的微小抖动 (Jiggle Animation)
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle_transition")

    // 生成一个 0~120 毫秒的随机错落值，让列表里不同的卡片摇摆节奏不一致，显得更自然生动
    val randomOffset = remember { (0..120).random() }

    val rotation by infiniteTransition.animateFloat(
        initialValue = -0.6f, // 向左倾斜极小的角度
        targetValue = 0.6f,   // 向右倾斜极小的角度
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
            // ✨ 将抖动动画通过 graphicsLayer 应用于 Z 轴旋转
            .graphicsLayer {
                if (isSelectionMode) {
                    rotationZ = rotation
                    // 可选：让卡片稍微放大一点点 (1.01倍)，更强化“被拿起来”的感觉
                    scaleX = 1.01f
                    scaleY = 1.01f
                }
            }
            .bounceClick(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation) // 使用动态阴影
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

            // 左侧推入的选择圆圈 (保持上一版的高级滑入效果)
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
                        .background(if (isSelected) AccentBlue else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) AccentBlue else Color(0xFFD1D1D6),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 原始图标区域
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
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val timeString = data.originalLedger?.let {
                    SimpleDateFormat("HH:mm", getDefault()).format(Date(it.timestamp))
                } ?: "12:00"

                Text(timeString, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.width(8.dp))

            // 金额区域
            val amountColor = if (data.amount.contains("+")) Color(0xFF00B42A) else Color(0xFFDB1B1B)
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