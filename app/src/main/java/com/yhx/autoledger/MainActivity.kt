package com.yhx.autoledger

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.ui.components.DoubleBackToExitHandler
import com.yhx.autoledger.ui.components.MainBottomBar
import com.yhx.autoledger.ui.components.ManualAddSheet
import com.yhx.autoledger.ui.components.PatternLock
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.navigation.Screen
import com.yhx.autoledger.ui.screens.AIScreen
import com.yhx.autoledger.ui.screens.AiMemoryManageScreen
import com.yhx.autoledger.ui.screens.BookManageScreen
import com.yhx.autoledger.ui.screens.CategoryManageScreen
import com.yhx.autoledger.ui.screens.DetailScreen
import com.yhx.autoledger.ui.screens.HomeScreen
import com.yhx.autoledger.ui.screens.SettingsScreen
import com.yhx.autoledger.ui.screens.DataImportExportScreen
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.ui.theme.AutoLedgerTheme
import com.yhx.autoledger.viewmodel.HomeViewModel
import com.yhx.autoledger.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() { // ✨ 必须是 FragmentActivity 才能调起生物识别

    @Inject
    lateinit var categoryDao: CategoryDao

    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        lifecycleScope.launch {
            categoryDao.getAllCategories().collect { categories ->
                Log.d("DB_TEST", "数据库被唤醒啦！当前有 ${categories.size} 个分类")
            }
        }

        setContent {
            val themePreference by mainViewModel.themePreference.collectAsState()
            val currentBookId by mainViewModel.currentBookId.collectAsState()
            val currentBook by mainViewModel.currentBook.collectAsState()

            // ✨ 获取隐私锁和提醒状态
            val privacyLockEnabled by mainViewModel.privacyLockEnabled.collectAsState()
            val privacyLockPattern by mainViewModel.privacyLockPattern.collectAsState() // ✨ 新增获取密码
            val reminderTime by mainViewModel.reminderTime.collectAsState()
            val context = LocalContext.current
            // ✨ 获取人设状态
            val aiPersonaId by mainViewModel.aiPersonaId.collectAsState()
            val allPersonas = mainViewModel.allPersonas

            // ✨ 核心生命周期监听：App 切后台即重置验证状态
            var isAuthenticated by rememberSaveable { mutableStateOf(false) }
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner, privacyLockEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    // 当应用退到后台时，重置认证状态（锁死）
                    if (event == Lifecycle.Event.ON_STOP) {
                        isAuthenticated = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // ✨ 只有开启了开关，并且真的有密码，且未认证时，才显示锁屏
            val showLockScreen = privacyLockEnabled && privacyLockPattern.isNotEmpty() && !isAuthenticated

            AutoLedgerTheme(themePreference = themePreference) {

                // ✨ 顶级拦截：如果需要锁屏，直接覆盖全屏 UI，不渲染底部导航
                if (showLockScreen) {
                    PrivacyLockScreen(
                        savedPattern = privacyLockPattern, // 👈 传真实密码
                        onUnlock = { isAuthenticated = true }
                    )
                } else {
                    // 👇 原本正常的 NavHost 导航逻辑
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "main_tabs"
                    ) {
                        composable("main_tabs") {
                            DoubleBackToExitHandler()
                            val tabOrder = remember {
                                listOf(Screen.Home, Screen.Detail, Screen.AI, Screen.Settings)
                            }

                            val pagerState = rememberPagerState(pageCount = { tabOrder.size })
                            val coroutineScope = rememberCoroutineScope()
                            var showAddSheet by remember { mutableStateOf(false) }

                            var isPagerScrollable by remember { mutableStateOf(true) }

                            Scaffold(
                                bottomBar = {
                                    MainBottomBar(
                                        currentRoute = tabOrder[pagerState.currentPage].route,
                                        onNavigate = { route ->
                                            val targetIndex = tabOrder.indexOfFirst { it.route == route }
                                            if (targetIndex != -1) {
                                                coroutineScope.launch {
                                                    pagerState.scrollToPage(targetIndex)
                                                }
                                            }
                                        }
                                    )
                                },
                                floatingActionButton = {
                                    if (pagerState.currentPage == 0 || pagerState.currentPage == 1) {
                                        FloatingActionButton(
                                            onClick = { showAddSheet = true },
                                            containerColor = AppDesignSystem.colors.brandAccent,
                                            contentColor = Color.White,
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .padding(bottom = 16.dp)
                                                .bounceClick()
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                "手动记账",
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                },
                                containerColor = AppDesignSystem.colors.appBackground
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .consumeWindowInsets(innerPadding)
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize(),
                                        beyondBoundsPageCount = 2,
                                        userScrollEnabled = isPagerScrollable
                                    ) { pageIndex ->
                                        when (tabOrder[pageIndex].route) {
                                            Screen.Home.route -> HomeScreen()

                                            Screen.Detail.route -> DetailScreen(
                                                onSubPageVisible = { isSubPageVisible ->
                                                    isPagerScrollable = !isSubPageVisible
                                                }
                                            )

                                            Screen.AI.route -> AIScreen()

                                            Screen.Settings.route -> SettingsScreen(
                                                currentTheme = themePreference,
                                                currentBookName = currentBook?.name ?: "读取中...",
                                                // ✨ 传递所有参数
                                                privacyLockEnabled = privacyLockEnabled,
                                                privacyLockPattern = privacyLockPattern, // ✨
                                                reminderTime = reminderTime,
                                                // ✨ 新增：传入人设参数
                                                aiPersonaId = aiPersonaId,
                                                allPersonas = allPersonas,
                                                onSetAiPersonaId = { mainViewModel.setAiPersonaId(it) },

                                                onTogglePrivacyLock = { mainViewModel.setPrivacyLock(it) },
                                                onSetPrivacyPattern = { mainViewModel.setPrivacyPattern(it) }, // ✨
                                                onSetReminderTime = { mainViewModel.setReminderTime(context, it) },

                                                onThemeChange = { mainViewModel.updateTheme(it) },
                                                onNavigateToImportExport = { navController.navigate(Screen.DataImportExport.route) },
                                                onNavigateToCategoryManage = { navController.navigate(Screen.CategoryManage.route) },
                                                onNavigateToBookManage = { navController.navigate(Screen.BookManage.route) },
                                                onNavigateToAiMemory = { navController.navigate(Screen.AiMemoryManage.route) }
                                            )
                                        }
                                    }
                                }

                                val homeViewModel: HomeViewModel = hiltViewModel()

                                if (showAddSheet) {
                                    ManualAddSheet(
                                        onDismiss = { showAddSheet = false },
                                        onSave = { type, category, icon, amount, remark, timestamp ->
                                            val parsedAmount = amount.toDoubleOrNull() ?: 0.0

                                            homeViewModel.addLedger(
                                                bookId = currentBookId,
                                                amount = parsedAmount,
                                                type = type,
                                                categoryName = category,
                                                categoryIcon = icon,
                                                timestamp = timestamp,
                                                note = remark
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        composable(Screen.DataImportExport.route) {
                            DataImportExportScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.CategoryManage.route) {
                            CategoryManageScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.BookManage.route) {
                            BookManageScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.AiMemoryManage.route) {
                            AiMemoryManageScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

// ✨ 九宫格安全解锁覆盖层
@Composable
fun PrivacyLockScreen(savedPattern: String, onUnlock: () -> Unit) {
    var isError by remember { mutableStateOf(false) }

    // ✨ 核心修复：当发生错误时，延迟 1 秒钟把错误状态重置为 false，解冻输入！
    LaunchedEffect(isError) {
        if (isError) {
            kotlinx.coroutines.delay(1000)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDesignSystem.colors.appBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = "已锁定",
                modifier = Modifier.size(64.dp),
                tint = if (isError) AppDesignSystem.colors.warningRed else AppDesignSystem.colors.brandAccent
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isError) "图案错误，请重试" else "请输入解锁图案",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isError) AppDesignSystem.colors.warningRed else AppDesignSystem.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(48.dp))

            // ✨ 调用我们刚才写的九宫格组件
            PatternLock(
                isError = isError,
                onPatternComplete = { drawnList ->
                    val drawnStr = drawnList.joinToString(",")
                    if (drawnStr == savedPattern) {
                        isError = false
                        onUnlock() // 密码正确，放行！
                    } else {
                        isError = true // 密码错误，飘红并重置
                    }
                }
            )
        }
    }
}