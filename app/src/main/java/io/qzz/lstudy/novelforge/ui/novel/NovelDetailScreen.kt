package io.qzz.lstudy.novelforge.ui.novel

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.qzz.lstudy.novelforge.data.local.entity.Chapter
import kotlinx.coroutines.launch

/**
 * 小说详情页
 *
 * 包含：
 * 1. 顶部 AI 抽屉图标（点击从左滑出对话面板）
 * 2. 小说信息卡片（标题、字数进度、token 总量）
 * 3. 生成模式按钮（逐章 / 全部）
 * 4. 章节列表
 * 5. 章节阅读/编辑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailScreen(
    viewModel: NovelDetailViewModel,
    novelId: Long,
    onBack: () -> Unit
) {
    LaunchedEffect(novelId) { viewModel.load(novelId) }

    val novel by viewModel.novel.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val generateState by viewModel.generateState.collectAsState()
    val dialogMessages by viewModel.dialogMessages.collectAsState()
    val dialogLoading by viewModel.dialogLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editingChapter by remember { mutableStateOf<Chapter?>(null) }
    var readingChapter by remember { mutableStateOf<Chapter?>(null) }
    var showModeDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    // 监听生成状态
    LaunchedEffect(generateState) {
        when (val s = generateState) {
            is GenerateState.Success -> {
                snackbarHostState.showSnackbar("生成完成：${s.chapterTitle}（消耗 ${s.tokensUsed} tokens）")
                viewModel.resetGenerateState()
            }
            is GenerateState.Error -> {
                snackbarHostState.showSnackbar("生成失败：${s.message}")
                viewModel.resetGenerateState()
            }
            else -> Unit
        }
    }

    // 章节阅读页（优先级最高）
    readingChapter?.let { ch ->
        ChapterReaderScreen(
            chapter = ch,
            onBack = { readingChapter = null },
            onEdit = { editingChapter = ch; readingChapter = null },
            onOpenAiDrawer = openDrawer
        )
        return
    }

    // 章节编辑对话框
    editingChapter?.let { ch ->
        ChapterEditDialog(
            chapter = ch,
            onDismiss = { editingChapter = null },
            onSave = { newContent ->
                viewModel.updateChapterContent(ch, newContent)
                editingChapter = null
            }
        )
        return
    }

    // AI 对话抽屉容器
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AiDialogDrawer(
                messages = dialogMessages,
                loading = dialogLoading,
                currentChapter = readingChapter,
                onSend = { msg -> viewModel.sendDialogMessage(readingChapter, msg) },
                onClear = { viewModel.clearDialog() },
                onClose = closeDrawer
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(novel?.title ?: "小说详情") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        // AI 调用图标（双子星样式）
                        IconButton(onClick = openDrawer) {
                            Icon(Icons.Default.SmartToy, contentDescription = "AI 对话")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 小说信息卡片（含 token 用量）
                novel?.let { n ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                n.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "已写 ${n.currentWords} / ${n.targetWords} 字",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val progress = if (n.targetWords > 0) {
                                n.currentWords.toFloat() / n.targetWords.toFloat()
                            } else 0f
                            Text(
                                "进度：${"%.1f".format(progress * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            // token 用量
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Token 消耗：${n.totalTokens}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (n.model.isNotBlank()) {
                                Text(
                                    "模型：${n.model}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // 生成按钮区域
                Button(
                    onClick = { showModeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = generateState !is GenerateState.Loading
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("生成章节")
                }

                // 生成进度
                if (generateState is GenerateState.Loading) {
                    val s = generateState as GenerateState.Loading
                    Column {
                        Text(s.message, style = MaterialTheme.typography.bodySmall)
                        if (s.total > 0) {
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (s.total > 0) s.current.toFloat() / s.total else 0f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                // 章节列表
                Text("章节列表", style = MaterialTheme.typography.titleMedium)
                Text(
                    "共 ${chapters.size} 章",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (chapters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text("暂无章节", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            Text("点击上方「生成章节」开始创作", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chapters, key = { it.id }) { ch ->
                            ChapterItem(
                                chapter = ch,
                                onClick = { readingChapter = ch },
                                onEdit = { editingChapter = ch },
                                onDelete = { viewModel.deleteChapter(ch) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 生成模式选择对话框
    if (showModeDialog) {
        GenerateModeDialog(
            onDismiss = { showModeDialog = false },
            onGenerate = { mode ->
                showModeDialog = false
                viewModel.generateNextChapter(mode, novel?.targetChapters ?: 0)
            }
        )
    }
}

/**
 * AI 对话抽屉
 * 从左滑出，可输入提示词与 AI 多轮对话
 */
@Composable
fun AiDialogDrawer(
    messages: List<DialogMessage>,
    loading: Boolean,
    currentChapter: Chapter?,
    onSend: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .padding(16.dp)
        ) {
            // 标题栏
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("AI 对话", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Clear, contentDescription = "关闭")
                }
            }
            if (currentChapter != null) {
                Text(
                    "当前章节：${currentChapter.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Text(
                    "提示：进入章节阅读后可针对章节内容对话",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 消息列表
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { m ->
                    DialogMessageItem(m)
                }
                if (loading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 清空按钮
            if (messages.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("清空对话") }
            }

            // 输入框
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入提示词...") },
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSend(input)
                            input = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
                }
            }
        }
    }
}

/** 对话消息项 */
@Composable
fun DialogMessageItem(message: DialogMessage) {
    val isUser = message.role == "user"
    val bg = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                message.content,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 章节阅读页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    chapter: Chapter,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenAiDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第${chapter.order}章 · ${chapter.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = onOpenAiDrawer) {
                        Icon(Icons.Default.SmartToy, contentDescription = "AI 对话")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "第${chapter.order}章 ${chapter.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                chapter.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "（本章 ${chapter.content.length} 字）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/** 章节列表项 */
@Composable
fun ChapterItem(
    chapter: Chapter,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "第${chapter.order}章 · ${chapter.title}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${chapter.content.length} 字",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    chapter.content.take(60).replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * 章节编辑对话框
 */
@Composable
fun ChapterEditDialog(
    chapter: Chapter,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var content by remember(chapter.id) { mutableStateOf(chapter.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("第${chapter.order}章 · ${chapter.title}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .verticalScroll(rememberScrollState()),
                    label = { Text("章节正文") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(content) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 生成模式选择对话框
 */
@Composable
fun GenerateModeDialog(
    onDismiss: () -> Unit,
    onGenerate: (GenerateMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择生成模式") },
        text = {
            Column {
                Text(
                    "逐章生成：每次只生成一章，可随时审阅后再续写",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "一次性生成全部：按目标章节数连续生成（耗时较长，请确保 API 额度充足）",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Column {
                Button(
                    onClick = { onGenerate(GenerateMode.CHAPTER_BY_CHAPTER) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("逐章生成下一章") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onGenerate(GenerateMode.ALL_AT_ONCE) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("一次性生成全部") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
