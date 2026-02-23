package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.ui.components.AdvancedChatInput
import com.yhx.autoledger.ui.components.BaseTransactionSheet
import com.yhx.autoledger.ui.components.ChatAvatar
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.viewmodel.AIViewModel
import kotlinx.coroutines.launch

@Composable
fun AIScreen(viewModel: AIViewModel = hiltViewModel()) {

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var editingState by remember { mutableStateOf<Pair<String, BillPreview>?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val isNearBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= (messages.size - 3).coerceAtLeast(0)
        }
    }

    var lastReadCount by remember { mutableIntStateOf(messages.size) }

    LaunchedEffect(isNearBottom, messages.size) {
        if (isNearBottom) {
            lastReadCount = messages.size
        }
    }

    val unreadCount = (messages.size - lastReadCount).coerceAtLeast(0)

    var isInitialScrollDone by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            if (!isInitialScrollDone) {
                listState.scrollToItem(messages.size - 1)
                isInitialScrollDone = true
            } else if (isNearBottom) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    var isInputVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousOffset by remember { mutableIntStateOf(0) }
    val scrollThreshold = 40

    LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }.collect { (index, offset) ->
            if (index != previousIndex) {
                isInputVisible = index > previousIndex
                previousIndex = index
                previousOffset = offset
            } else {
                val delta = offset - previousOffset
                if (delta > scrollThreshold) {
                    isInputVisible = true
                    previousOffset = offset
                } else if (delta < -scrollThreshold) {
                    isInputVisible = false
                    previousOffset = offset
                }
            }
        }
    }

    val meshGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFE0F2F1), Color(0xFFF7F9FC)),
        center = androidx.compose.ui.geometry.Offset(200f, 200f),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(meshGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AIHeader()

            // ✨ 修改点 1：将 Box 作为核心布局，让输入框悬浮在列表之上，不再改变列表高度
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    // ✨ 增加 bottom padding，为悬浮的输入框留出空间，防止最后一条消息被遮挡
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(messages) { msg ->
                        AdvancedChatBubble(
                            msg = msg,
                            onSave = { msgId, preview ->
                                viewModel.confirmAndSaveLedger(msgId, preview)
                            },
                            onEdit = { msgId, preview -> editingState = msgId to preview }
                        )
                    }
                    if (isLoading) {
                        item {
                            Text(
                                "AI 正在思考中...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                // ✨ 修改点 2：改回动态胶囊按钮，高度固定为 40.dp，保持精致感
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isNearBottom && messages.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    // 为了不和底部的输入框重叠，稍微把按钮往上挪一点点
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isInputVisible || isNearBottom) 80.dp else 16.dp, end = 16.dp)
                ) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        },
                        // ✨ 核心修复：限制高度 40dp。如果没有未读，强制宽度也为 40dp (变正圆)；
                        // 并加上 animateContentSize 自动处理宽度的丝滑伸缩动画
                        modifier = Modifier
                            .height(40.dp)
                            .then(if (unreadCount == 0) Modifier.width(40.dp) else Modifier)
                            .animateContentSize(),
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(
                            // 展开时左右留一点 padding，收回成圆形时 padding 为 0 保证图标绝对居中
                            modifier = Modifier.padding(horizontal = if (unreadCount > 0) 12.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "回到最新",
                                modifier = Modifier.size(20.dp)
                            )

                            // 有未读消息时，才显示文字
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$unreadCount 条新消息",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1 // 保证文字不换行
                                )
                            }
                        }
                    }
                }

                // ✨ 修改点 3：输入框变为悬浮态 (Alignment.BottomCenter)，彻底消灭推挤带来的白边卡顿
                androidx.compose.animation.AnimatedVisibility(
                    visible = isInputVisible || isNearBottom,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AdvancedChatInput(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                isInputVisible = true
                                focusManager.clearFocus()
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        editingState?.let { (msgId, preview) ->
            // ... 底部的 BaseTransactionSheet 逻辑保持完全不变
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val initialTimestamp = try {
                format.parse(preview.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            BaseTransactionSheet(
                isEditMode = true,
                initialType = preview.type,
                initialAmount = preview.amount,
                initialCategory = preview.category,
                initialIcon = preview.icon,
                initialRemark = preview.note.ifBlank { "" },
                initialTimestamp = initialTimestamp,
                onDismiss = { editingState = null },
                onDelete = null,
                onSave = { type, category, icon, amountDouble, remark, timestampLong ->
                    val amountStr = if (amountDouble % 1.0 == 0.0) amountDouble.toInt()
                        .toString() else amountDouble.toString()
                    val updatedDateStr = format.format(java.util.Date(timestampLong))

                    val updatedPreview = preview.copy(
                        type = type,
                        category = category,
                        icon = icon,
                        amount = amountStr,
                        note = remark,
                        date = updatedDateStr
                    )
                    viewModel.updateMessagePreview(msgId, updatedPreview)
                    editingState = null
                }
            )
        }
    }
}

@Composable
fun AIHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "AI 记账管家",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AdvancedChatBubble(
    msg: ChatMessage,
    onSave: (String, BillPreview) -> Unit,
    onEdit: (String, BillPreview) -> Unit
) {
    val arrangement = if (msg.isFromUser) Arrangement.End else Arrangement.Start

    // 气泡形状保持不变，但圆角可以稍微收紧一点，配合更小的气泡
    val bubbleShape = if (msg.isFromUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    val bubbleColor = if (msg.isFromUser) AccentBlue else Color(0xFFF5F5F7)
    val textColor = if (msg.isFromUser) Color.White else Color(0xFF1D1D1F)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // 外部列表已经有 spacedBy，这里稍微留一点即可
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Top
    ) {
        if (!msg.isFromUser) {
            ChatAvatar(isFromUser = false)
            Spacer(Modifier.width(8.dp)) // 稍微缩短头像和气泡的距离
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (msg.isFromUser) Alignment.End else Alignment.Start
        ) {
            // 文字气泡
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                // ✨ 统一将最大宽度收缩，增加屏幕两侧留白
                modifier = Modifier.widthIn(max = 236.dp)
            ) {
                Text(
                    text = msg.content,
                    // ✨ 减小内边距，让气泡更贴合文字
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = textColor,
                    // ✨ 字号减小，增加行高和字间距，提升精致感
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // 渲染账单卡片
            if (msg.billPreviews.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                msg.billPreviews.forEach { preview ->
                    AnimatedVisibility(visible = true) {
                        ReceiptCard(
                            preview = preview,
                            onConfirm = { onSave(msg.id, preview) },
                            onEdit = { onEdit(msg.id, preview) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (msg.isFromUser) {
            Spacer(Modifier.width(8.dp))
            ChatAvatar(isFromUser = true)
        }
    }
}

@Composable

fun ReceiptCard(preview: BillPreview, onConfirm: () -> Unit, onEdit: () -> Unit) {
    val isConfirmed = preview.isSaved
    val displayDate = remember(preview.date) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateObj = sdf.parse(preview.date)
            val outSdf = java.text.SimpleDateFormat("MM月dd日 EEEE", java.util.Locale.CHINESE)
            if (dateObj != null) outSdf.format(dateObj) else preview.date
        } catch (e: Exception) {
            preview.date
        }
    }

    // ✨ 高级感核心：移除边框，使用柔和的弥散阴影，纯白底色
    Surface(
        modifier = Modifier
            .width(236.dp)
            .padding(vertical = 4.dp), // 为阴影留出空间
        shape = RoundedCornerShape(24.dp), // 更大的圆角
        color = Color.White,
        shadowElevation = 8.dp,
        // 模拟弥散阴影，去掉系统默认的黑色硬阴影感
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            // 右上角编辑按钮 (扁平化，融入背景)
            if (!isConfirmed) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "修改",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                // 顶部：图标与金额区域（更加紧凑现代）
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = preview.color.copy(alpha = 0.15f), // 更淡的背景色
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(preview.icon, fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (preview.type == 0) "支出" else "收入",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93), // Apple 标准次级灰
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "¥ ${preview.amount}",
                            fontSize = 26.sp, // 金额更大
                            fontWeight = FontWeight.ExtraBold, // 字重加粗
                            color = Color(0xFF1D1D1F) // 苹果常用的高级深灰，取代纯黑
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ✨ 高级细节：绘制一条虚线分割线，模拟真实票据
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = Color(0xFFE5E5EA),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 中部：详情列表（移除灰色背景块，保持极简白）
                Column(modifier = Modifier.fillMaxWidth()) {
                    val finalNote = preview.note.ifBlank { preview.category }
                    DetailRow(label = "备注", value = finalNote)
                    DetailRow(label = "分类", value = preview.category)
                    DetailRow(label = "日期", value = displayDate)
                }

                Spacer(Modifier.height(24.dp))

                // 底部按钮：优化圆角和点击状态
                Button(
                    onClick = onConfirm,
                    enabled = !isConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = Color(0xFFF2F2F7), // 极其淡的灰色
                        disabledContentColor = Color(0xFFC7C7CC) // 禁用的文字颜色
                    ),
                    shape = RoundedCornerShape(14.dp), // 按钮圆角与卡片呼应
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ) // 扁平化按钮更现代
                ) {
                    Text(
                        text = if (isConfirmed) "已归档" else "确认归档",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFFAEAEC0))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.SemiBold
        )
    }
}


