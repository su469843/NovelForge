package io.qzz.lstudy.novelforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.ui.home.CreateNovelDialog
import io.qzz.lstudy.novelforge.ui.home.HomeViewModel
import io.qzz.lstudy.novelforge.ui.settings.SettingsScreen
import io.qzz.lstudy.novelforge.ui.settings.SettingsViewModel
import io.qzz.lstudy.novelforge.ui.theme.NovelForgeTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 页面导航枚举 */
private enum class Screen { Home, Settings }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()
            NovelForgeTheme(themeName = appTheme) {
                NovelForgeApp(
                    homeViewModel = homeViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun NovelForgeApp(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val novels by homeViewModel.novels.collectAsStateWithLifecycle()
    val skills by homeViewModel.skills.collectAsStateWithLifecycle()
    val apiKeys by settingsViewModel.apiKeys.collectAsStateWithLifecycle()
    val exportMode by settingsViewModel.exportMode.collectAsStateWithLifecycle()
    val appTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()

    // 根据窗口宽度判断布局：平板用侧边栏，手机用全屏切换
    val windowWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val isTablet = windowWidth >= 600

    if (isTablet) {
        TabletLayout(
            novels = novels,
            currentScreen = currentScreen,
            onScreenChange = { currentScreen = it },
            onNovelClick = { /* TODO */ },
            onDeleteNovel = { homeViewModel.deleteNovel(it) },
            onAddClick = { showCreateDialog = true },
            apiKeys = apiKeys,
            exportMode = exportMode,
            appTheme = appTheme,
            onSetApiKey = { p, k -> settingsViewModel.setApiKey(p, k) },
            onSetExportMode = { settingsViewModel.setExportMode(it) },
            onSetTheme = { settingsViewModel.setTheme(it) }
        )
    } else {
        when (currentScreen) {
            Screen.Home -> PhoneHomeScreen(
                novels = novels,
                onNovelClick = { /* TODO */ },
                onDeleteNovel = { homeViewModel.deleteNovel(it) },
                onAddClick = { showCreateDialog = true },
                onSettingsClick = { currentScreen = Screen.Settings }
            )
            Screen.Settings -> SettingsScreen(
                apiKeys = apiKeys,
                exportMode = exportMode,
                currentTheme = appTheme,
                onBack = { currentScreen = Screen.Home },
                onSetApiKey = { p, k -> settingsViewModel.setApiKey(p, k) },
                onSetExportMode = { settingsViewModel.setExportMode(it) },
                onSetTheme = { settingsViewModel.setTheme(it) }
            )
        }
    }

    if (showCreateDialog) {
        CreateNovelDialog(
            skills = skills,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, targetWords, _ ->
                scope.launch {
                    homeViewModel.createNovel(title, targetWords)
                    showCreateDialog = false
                }
            }
        )
    }
}

// ===================== 平板侧边栏布局 =====================

@Composable
fun TabletLayout(
    novels: List<Novel>,
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onNovelClick: (Novel) -> Unit,
    onDeleteNovel: (Novel) -> Unit,
    onAddClick: () -> Unit,
    apiKeys: Map<String, String>,
    exportMode: String,
    appTheme: String,
    onSetApiKey: (String, String) -> Unit,
    onSetExportMode: (String) -> Unit,
    onSetTheme: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 侧边栏
        Surface(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                // 标题
                Text(
                    "NovelForge",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                )
                Spacer(Modifier.height(8.dp))

                // 新建小说按钮
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "新建小说",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 小说列表
                Text(
                    "我的小说",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(4.dp))

                if (novels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无小说",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(novels, key = { it.id }) { novel ->
                            SidebarNovelItem(
                                novel = novel,
                                onClick = { onNovelClick(novel) },
                                onDelete = { onDeleteNovel(novel) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 设置按钮（底部）
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScreenChange(Screen.Settings) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentScreen == Screen.Settings)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // 右侧内容区
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (currentScreen) {
                Screen.Home -> TabletHomeContent(
                    novels = novels,
                    onNovelClick = onNovelClick,
                    onDeleteNovel = onDeleteNovel,
                    onAddClick = onAddClick
                )
                Screen.Settings -> SettingsScreen(
                    apiKeys = apiKeys,
                    exportMode = exportMode,
                    currentTheme = appTheme,
                    onBack = { onScreenChange(Screen.Home) },
                    onSetApiKey = onSetApiKey,
                    onSetExportMode = onSetExportMode,
                    onSetTheme = onSetTheme
                )
            }
        }
    }
}

/** 侧边栏中的小说条目 */
@Composable
fun SidebarNovelItem(
    novel: Novel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Book,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                novel.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${novel.currentWords} / ${novel.targetWords} 字",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** 平板模式下的主页内容区（空状态或小说网格） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletHomeContent(
    novels: List<Novel>,
    onNovelClick: (Novel) -> Unit,
    onDeleteNovel: (Novel) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新建小说")
            }
        }
    ) { innerPadding ->
        if (novels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "选择左侧小说或点击 + 新建",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(novels, key = { it.id }) { novel ->
                    NovelCard(
                        novel = novel,
                        onClick = { onNovelClick(novel) },
                        onDelete = { onDeleteNovel(novel) }
                    )
                }
            }
        }
    }
}

/** 手机模式下的主页（保留原 HomeScreen 逻辑） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneHomeScreen(
    novels: List<Novel>,
    onNovelClick: (Novel) -> Unit,
    onDeleteNovel: (Novel) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的小说") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Text(
                        "设置",
                        modifier = Modifier
                            .clickable { onSettingsClick() }
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新建小说")
            }
        }
    ) { innerPadding ->
        if (novels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "还没有小说",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "点击右下角 + 按钮开始创作",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(novels, key = { it.id }) { novel ->
                    NovelCard(
                        novel = novel,
                        onClick = { onNovelClick(novel) },
                        onDelete = { onDeleteNovel(novel) }
                    )
                }
            }
        }
    }
}

/** 小说卡片（手机与平板共用） */
@Composable
fun NovelCard(
    novel: Novel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    novel.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "已写 ${novel.currentWords} / ${novel.targetWords} 字",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatDate(novel.createTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}