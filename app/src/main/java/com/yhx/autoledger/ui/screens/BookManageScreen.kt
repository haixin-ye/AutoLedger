package com.yhx.autoledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.AccountBookEntity
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.viewmodel.BookManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookManageScreen(
    onBack: () -> Unit,
    viewModel: BookManageViewModel = hiltViewModel()
) {
    val books by viewModel.allBooks.collectAsState()
    val currentBookId by viewModel.currentBookId.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppDesignSystem.colors.appBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("账本管理", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "返回", tint = AppDesignSystem.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppDesignSystem.colors.appBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = AppDesignSystem.colors.brandAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.bounceClick()
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增账本")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            items(books, key = { it.id }) { book ->
                val isSelected = book.id == currentBookId
                val bookColor = Color(book.coverColor)

                // 账本卡片
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppDesignSystem.colors.cardBackground)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) bookColor else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.switchBook(book.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 账本颜色封面块
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bookColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📓", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    // 账本名称
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDesignSystem.colors.textPrimary
                        )
                        if (book.isSystemDefault) {
                            Text("预设账本", fontSize = 12.sp, color = AppDesignSystem.colors.textTertiary)
                        }
                    }

                    // 右侧状态与操作
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "当前选中", tint = bookColor)
                    } else if (!book.isSystemDefault) {
                        // 允许删除自定义账本
                        IconButton(onClick = { viewModel.deleteBook(book) }) {
                            Icon(Icons.Rounded.DeleteOutline, "删除", tint = AppDesignSystem.colors.warningRed)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) } // 底部留白给 FAB
        }
    }

    if (showAddSheet) {
        AddBookSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, color ->
                viewModel.addBook(name, color.toArgb())
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, color: Color) -> Unit
) {
    var bookName by remember { mutableStateOf("") }
    // 预设几种好看的颜色
    val colorOptions = listOf(
        Color(0xFF42A5F5), Color(0xFFFFA726), Color(0xFF66BB6A),
        Color(0xFFAB47BC), Color(0xFFEF5350), Color(0xFF26A69A)
    )
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppDesignSystem.colors.sheetBackground,
        windowInsets = WindowInsets.ime // ✨ 防软键盘遮挡
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text("新增账本", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textPrimary)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = bookName,
                    onValueChange = { if (it.length <= 8) bookName = it },
                    placeholder = { Text("给账本起个名字 (最多8字)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        focusedTextColor = AppDesignSystem.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("选择封面颜色", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 4.dp else 0.dp,
                                    color = if (selectedColor == color) AppDesignSystem.colors.textPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { onSave(bookName, selectedColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(50.dp)
                    .bounceClick(),
                enabled = bookName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("创建并保存", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}