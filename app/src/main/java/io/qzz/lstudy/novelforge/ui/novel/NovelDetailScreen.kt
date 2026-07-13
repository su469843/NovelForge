package io.qzz.lstudy.novelforge.ui.novel

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.qzz.lstudy.novelforge.data.local.entity.Chapter

/**
 * 小说详情页
 *
 * 包含：
 * 1. 小说信息卡片（标题、字数进度）
 * 2. 章节列表
 * 3. 「AI 生成下一章」按钮（核心生成入口）
 * 4. 点击章节进入编辑页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailScreen(
    viewModel: NovelDetailViewModel,
    novelId: Long,
    onBack: () -> Unit
) {
    // 加载小说数据
    LaunchedEffect(novelId) {
        viewModel.load(novelId)
    }

    val novel by viewModel.novel.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val generateState by viewModel.generateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 编辑模式状态
    var editingChapter by remember { mutableStateOf<Chapter?>(null) }

    // 监听生成状态，显示提示
    LaunchedEffect(generateState) {
        when (val s = generateState) {
            is GenerateState.Success -> {
                snackbarHostState.showSnackbar("已生成章节：${s.chapterTitle}")
                viewModel.resetGenerateState()
            }
            is GenerateState.Error -> {
                snackbarHostState.showSnackbar("生成失败：${s.message}")
                viewModel.resetGenerateState()
            }
            else -> Unit
        }
    }

    // 章节编辑覆盖层
    editingChapter?.let { ch ->
        ChapterEditDialog(
            chapter = ch,
            onDismiss = { editingChapter = null },
            onSave = { newContent ->
                viewModel.updateChapterContent(ch, newContent)
                editingChapter = null
            }
        )
        return@let
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(novel?.title ?: "小说详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            // 小说信息卡片
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
                    }
                }
            }

            // AI 生成按钮（核心入口）
            Button(
                onClick = { viewModel.generateNextChapter() },
                modifier = Modifier.fillMaxWidth(),
                enabled = generateState !is GenerateState.Loading
            ) {
                if (generateState is GenerateState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(0.dp))
                    Text("  生成中...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("  AI 生成下一章")
                }
            }

            // 生成中的提示文案
            if (generateState is GenerateState.Loading) {
                Text(
                    (generateState as GenerateState.Loading).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 章节列表标题
            Text("章节列表", style = MaterialTheme.typography.titleMedium)
            Text(
                "共 ${chapters.size} 章",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            // 章节列表
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
                        Text(
                            "暂无章节",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "点击上方「AI 生成下一章」开始创作",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
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
                            onClick = { editingChapter = ch },
                            onDelete = { viewModel.deleteChapter(ch) }
                        )
                    }
                }
            }
        }
    }
}

/** 章节列表项 */
@Composable
fun ChapterItem(
    chapter: Chapter,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            IconButton(onClick = onClick) {
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
 * 可查看完整内容并修改
 */
@Composable
fun ChapterEditDialog(
    chapter: Chapter,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var content by remember(chapter.id) { mutableStateOf(chapter.content) }

    androidx.compose.material3.AlertDialog(
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
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
