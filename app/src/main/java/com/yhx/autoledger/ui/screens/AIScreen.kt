package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExtendedFloatingActionButton
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
    // ✨ 记录当前正在被编辑的消息，如果为 null 则不显示弹窗
    var editingState by remember { mutableStateOf<Pair<String, BillPreview>?>(null) }

    // ✨ 列表状态与协程
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    // ✨ 2. 智能判断：用户目前是否停留在最新消息区域 (最后 3 条内视作底部)
    val isNearBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= messages.size - 3
        }
    }

    // ✨ 记录用户最后一次在底部时，看到了几条消息
    var lastReadCount by remember { mutableIntStateOf(messages.size) }

    // ✨ 状态同步：只要用户滑到了底部，就把“已读数量”更新为当前的总消息数
    LaunchedEffect(isNearBottom, messages.size) {
        if (isNearBottom) {
            lastReadCount = messages.size
        }
    }

    // ✨ 计算未读数量 (总消息数 - 已读消息数)
    val unreadCount = (messages.size - lastReadCount).coerceAtLeast(0)


    // ✨ 新增核心逻辑：区分“初次进入/重新切回页面”和“停留在页面时来新消息”
    var isInitialScrollDone by remember { mutableStateOf(false) }

    // ✨ 自动滚动机制：只要来新消息了，且用户本来就在底部，就丝滑滚到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            if (!isInitialScrollDone) {
                // 1. 如果是刚切到这个页面（或者刚从数据库加载出历史消息）
                // 使用 scrollToItem 进行【无动画瞬间闪现】，防止用户看到列表往下滚的残影
                listState.scrollToItem(messages.size - 1)
                isInitialScrollDone = true
            } else if (isNearBottom) {
                // 2. 如果用户一直停留在该页面且处于底部，AI回复了新消息
                // 使用 animateScrollToItem 进行【丝滑滚动】
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // 模拟背景渐变（Mesh Gradient 效果）
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(messages) { msg ->
                        AdvancedChatBubble(
                            msg = msg,
                            onSave = { msgId, preview ->
                                viewModel.confirmAndSaveLedger(
                                    msgId,
                                    preview
                                )
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

                // ✨ 4. 升级为 ExtendedFloatingActionButton 动态胶囊按钮
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isNearBottom && messages.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                // 点击瞬间滑到最底部！
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        },
                        // ✨ 如果有未读消息，按钮自动拉长展开；如果没有未读，就是一个圆形箭头
                        expanded = unreadCount > 0,
                        icon = {
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "回到最新")
                        },
                        text = {
                            // ✨ 展开时显示的文字

                            Text(
                                "$unreadCount 条新消息",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                        },
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        // 让它看起来像一个圆润的胶囊
                        shape = CircleShape
                    )
                }
            }

            AdvancedChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        // 用户主动发消息时，强制滑到底部
                        coroutineScope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                }
            )
        }

        // ✨ 修复 3：正确解析 editingState，完成修改更
        editingState?.let { (msgId, preview) ->
            // 将 "yyyy-MM-dd" 转换为时间戳
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
                onDelete = null, // AI 预览账单阶段不需要删除按钮，直接关掉弹窗即可
                onSave = { type, category, icon, amountDouble, remark, timestampLong ->
                    // 1. 金额转回 String
                    val amountStr = if (amountDouble % 1.0 == 0.0) amountDouble.toInt().toString() else amountDouble.toString()
                    // 2. 时间戳转回 "yyyy-MM-dd"
                    val updatedDateStr = format.format(java.util.Date(timestampLong))

                    // 3. 组装新的 Preview 并传给 ViewModel
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
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)) {
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


