package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.R
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.ui.components.bounceClick
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

    // ✨ 自动滚动机制：只要来新消息了，且用户本来就在底部，就丝滑滚到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && isNearBottom) {
            listState.animateScrollToItem(messages.size - 1)
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

        // ✨ 修复 3：正确解析 editingState，完成修改更新
        editingState?.let { (msgId, preview) ->
            EditAIBillSheet(
                preview = preview,
                onDismiss = { editingState = null },
                onSave = { updatedPreview ->
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

@Composable
fun AdvancedChatInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(56.dp), // 稍微收紧一点高度，显得精致
        color = Color.White,
        shape = CircleShape,
        // ✨ 高级感核心：纯白背景配上一层非常克制的弥散阴影
        shadowElevation = 12.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 8.dp), // 左侧增加呼吸感，右侧留给发送按钮
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF1D1D1F)
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            "输入语音或文字记一笔...",
                            color = Color(0xFFC7C7CC),
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )

            // 发送按钮优化：平时微透明，有字时高亮
            val isInputEmpty = text.trim().isEmpty()
            IconButton(
                onClick = onSend,
                enabled = !isInputEmpty,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isInputEmpty) Color(0xFFF2F2F7) else AccentBlue,
                        shape = CircleShape
                    )
                    .bounceClick()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (isInputEmpty) Color(0xFFC7C7CC) else Color.White,
                    modifier = Modifier.size(18.dp) // 图标稍微调小，显得精致
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAIBillSheet(
    preview: BillPreview,
    onDismiss: () -> Unit,
    onSave: (BillPreview) -> Unit
) {
    // 状态初始化
    var transactionType by remember { mutableIntStateOf(preview.type) } // 0: 支出, 1: 收入
    var amountText by remember { mutableStateOf(preview.amount) }
    var noteText by remember { mutableStateOf(preview.note) }

    // 分类定义（参考你的 ManualAddSheet）
    val expenseCategories = listOf(
        "餐饮" to "🍱", "交通" to "🚗", "购物" to "🛒",
        "娱乐" to "🎮", "居住" to "🏠", "其他" to "⚙️"
    )
    val incomeCategories = listOf(
        "工资" to "💰", "理财" to "📈", "兼职" to "💼",
        "红包" to "🧧", "报销" to "🧾", "其他" to "💵"
    )

    val currentCategories = if (transactionType == 0) expenseCategories else incomeCategories
    // 如果当前的分类不在当前类型的列表里，默认选第一个
    var selectedCategory by remember(transactionType) {
        mutableStateOf(if (currentCategories.any { it.first == preview.category }) preview.category else currentCategories[0].first)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val symbolColor = if (transactionType == 0) AccentBlue else Color(0xFF4CAF50)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9FC), // 统一背景色
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(Modifier.padding(top = 12.dp, bottom = 8.dp).size(40.dp, 5.dp).background(Color(0xFFE5E5EA), CircleShape))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("微调 AI 提取的账单", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(Modifier.height(20.dp))

            // 1. 类型切换 (参考 ManualAddSheet 风格)
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                listOf("支出" to 0, "收入" to 1).forEach { (label, type) ->
                    val isSelected = transactionType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { transactionType = type }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.Black else Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. 金额输入区（高级感改版：大字展示）
            Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(if (transactionType == 0) "- ¥" else "+ ¥", fontSize = 24.sp, fontWeight = FontWeight.Black, color = symbolColor)
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 3. 分类网格选择 (学习自 ManualAddSheet)
            Text("选择分类", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(150.dp) // 固定高度保证呼吸感
            ) {
                items(currentCategories) { (name, icon) ->
                    val isSelected = selectedCategory == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategory = name }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) symbolColor else Color.White,
                            modifier = Modifier.size(48.dp),
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 22.sp)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(name, fontSize = 12.sp, color = if (isSelected) symbolColor else Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. 备注输入框
            PremiumTextField(value = noteText, onValueChange = { noteText = it }, label = "备注 (可选)")

            Spacer(Modifier.height(28.dp))

            // 5. 保存按钮
            Button(
                onClick = {
                    val updatedPreview = preview.copy(
                        amount = amountText,
                        category = selectedCategory,
                        type = transactionType,
                        icon = currentCategories.find { it.first == selectedCategory }?.second ?: "📝",
                        note = noteText
                    )
                    onSave(updatedPreview)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = symbolColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("完成修改", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ✨ 新增：剥离出来的高级感无边框输入组件
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 独立的 Label，悬浮在输入框左上方
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E8E93),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        // 使用 BasicTextField 彻底摆脱系统默认的边框和下划线
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF1D1D1F),
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F7), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp), // 内部的呼吸感
                    contentAlignment = Alignment.CenterStart
                ) {
                    innerTextField()
                }
            }
        )
    }
}

// ✨ 新增：专门渲染头像的组件
@Composable
fun ChatAvatar(isFromUser: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isFromUser) Color(0xFFE3F2FD) else Color(0xFFFFF3E0), // 背景色区分
        modifier = Modifier
            .size(34.dp) // 头像统一大小
            .shadow(2.dp, CircleShape) // 增加轻微的立体感
            .border(1.dp, Color.White, CircleShape) // 白色描边，显得更精致
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//             💡 TODO: 当你把真实的图片放进 res/drawable 后，把下面这段换成：
            Image(
                painter = painterResource(id = if (isFromUser) R.drawable.ic_user_avatar else R.drawable.ic_ai_avatar),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // 👇 在你换真实图片之前，先用系统自带的漂亮 Icon 顶替
//            Icon(
//                imageVector = if (isFromUser) Icons.Rounded.Person else Icons.Rounded.SmartToy,
//                contentDescription = "Avatar",
//                tint = if (isFromUser) AccentBlue else Color(0xFFFF9800),
//                modifier = Modifier.size(24.dp)
//            )
        }
    }
}