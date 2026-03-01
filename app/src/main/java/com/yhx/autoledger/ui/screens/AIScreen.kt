package com.yhx.autoledger.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
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
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.viewmodel.AIViewModel
import kotlinx.coroutines.launch


@SuppressLint("ConstantLocale")
private val inputSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
private val outputSdf = java.text.SimpleDateFormat("MM月dd日 EEEE", java.util.Locale.CHINESE)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIScreen(viewModel: AIViewModel = hiltViewModel()) {

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var editingState by remember { mutableStateOf<Pair<String, BillPreview>?>(null) }
    val isImeVisible = WindowInsets.isImeVisible

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // focusManager 获取到了，但我们现在要极其克制地使用它，避免键盘乱收
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val isNearBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= (messages.size - 3).coerceAtLeast(0)
        }
    }

    var lastReadCount by remember { mutableIntStateOf(messages.size) }
    val dashLineColor = AppDesignSystem.colors.dividerColor
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

    // ✨ 修复 2：为了保证“沉浸打字”，删除了滑动列表时强制收起键盘的逻辑。
    // 如果你坚持想要滑动收起键盘，请解开注释，但目前最流行的做法是让用户自己决定何时收起。
    /*
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }
    */

    var isInputVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousOffset by remember { mutableIntStateOf(0) }
    val scrollThreshold = 40

    LaunchedEffect(listState, isImeVisible) {
        androidx.compose.runtime.snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }.collect { (index, offset) ->

            // 👉 核心拦截：如果键盘打开了，强制显示输入框，并同步状态，直接跳过后面的隐藏判断
            if (isImeVisible) {
                isInputVisible = true
                previousIndex = index
                previousOffset = offset
                return@collect
            }

            // 下面是你原本的滑动隐藏逻辑（只有键盘收起时才生效）
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
//        colors = listOf(Color(0xFFE0F2F1), Color(0xFFF7F9FC)),
        colors = listOf(AppDesignSystem.colors.appBackground, AppDesignSystem.colors.appBackground),
        center = androidx.compose.ui.geometry.Offset(200f, 200f),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(meshGradient)
    ) {
        // ✨ 修复 1 核心区域：在最外层的 Column 加上 imePadding() 和 navigationBarsPadding()
        // 这样整个内容（包括聊天列表和输入框）就会被键盘老老实实地顶上去，再也不会被遮挡！
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .statusBarsPadding()

        ) {
            AIHeader()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    // bottom padding 留出输入框的高度，防止最后一条消息被输入框盖住
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 24.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(
                        items = messages,
                        key = { msg -> msg.id }
                    ) { msg ->
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
                                color = AppDesignSystem.colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isNearBottom && messages.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            bottom = if (isInputVisible || isNearBottom) 80.dp else 16.dp,
                            end = 16.dp
                        )
                ) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        },
                        // ✨ 修复 2：加入 interactionSource = null (或 MutableInteractionSource)，
                        // 确保点击这个悬浮球时，不会抢走输入框的焦点，键盘就不会掉下去！
                        interactionSource = remember { MutableInteractionSource() },
                        modifier = Modifier
                            .height(40.dp)
                            .then(if (unreadCount == 0) Modifier.width(40.dp) else Modifier)
                            .animateContentSize(),
                        containerColor = AppDesignSystem.colors.brandAccent,
                        contentColor = AppDesignSystem.colors.textOnAccent,
                        shape = CircleShape,
                        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (unreadCount > 0) 12.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "回到最新",
                                modifier = Modifier.size(20.dp)
                            )

                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$unreadCount 条新消息",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isInputVisible || isNearBottom || isImeVisible,
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
                                // ✨ 修复 2 核心：绝对不要在这里 clearFocus()！
                                // 这样发送消息后，光标依然在输入框里闪烁，键盘稳稳当当
                                // focusManager.clearFocus()
//                                coroutineScope.launch {
//                                    if (messages.isNotEmpty()) {
//                                        listState.animateScrollToItem(messages.size - 1)
//                                    }
//                                }
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
            Icons.Default.Lightbulb,
            contentDescription = null,
            tint = AppDesignSystem.colors.brandAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "AI 记账助手",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AppDesignSystem.colors.textPrimary
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

    val bubbleShape = if (msg.isFromUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    val bubbleColor =
        if (msg.isFromUser) AppDesignSystem.colors.chatUserBubble else AppDesignSystem.colors.chatAiBubble
    val textColor = if (msg.isFromUser) AppDesignSystem.colors.textOnAccent else AppDesignSystem.colors.chatAiText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Top
    ) {
        if (!msg.isFromUser) {
            ChatAvatar(isFromUser = false)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (msg.isFromUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                modifier = Modifier.widthIn(max = 236.dp)
            ) {
                Text(
                    text = msg.content,
                    color = textColor,
                    // ✨ 修复 3：字体微调。缩小到 15.sp（之前如果是16的话），行高加到 22.sp 增加呼吸感
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.5.sp,
                    // padding 稍微紧凑一点，配合小字体
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            if (msg.billPreviews.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                msg.billPreviews.forEach { preview ->
                    // ✨✨✨ 唯一修改的地方：去掉了 AnimatedVisibility 包裹，直接渲染卡片！
                    ReceiptCard(
                        preview = preview,
                        onConfirm = { onSave(msg.id, preview) },
                        onEdit = { onEdit(msg.id, preview) }
                    )
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
    val dashLineColor = AppDesignSystem.colors.dividerColor
    val displayDate = remember(preview.date) {
        try {
            // 注意：如果你的项目最低版本支持，推荐用 LocalDate；
            // 如果必须用 SimpleDateFormat，请尽量不要在列表项滚动时解析它，或者用一个全局静态工具类来处理
            val dateObj = inputSdf.parse(preview.date)
            if (dateObj != null) outputSdf.format(dateObj) else preview.date
        } catch (e: Exception) {
            preview.date
        }
    }

    val dashPathEffect = remember {
        androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    }

    Surface(
        modifier = Modifier
            .width(236.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = AppDesignSystem.colors.cardBackground,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
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
                        tint = AppDesignSystem.colors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = preview.color.copy(alpha = 0.15f),
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
                            color = AppDesignSystem.colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "¥ ${preview.amount}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppDesignSystem.colors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = dashLineColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = dashPathEffect
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    val finalNote = preview.note.ifBlank { preview.category }
                    DetailRow(label = "备注", value = finalNote)
                    DetailRow(label = "分类", value = preview.category)
                    DetailRow(label = "日期", value = displayDate)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    enabled = !isConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = AppDesignSystem.colors.surfaceVariant,
                        disabledContentColor = AppDesignSystem.colors.textTertiary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
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
        Text(text = label, fontSize = 14.sp, color = AppDesignSystem.colors.textSecondary)
        Text(
            text = value,
            fontSize = 14.sp,
            color = AppDesignSystem.colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}