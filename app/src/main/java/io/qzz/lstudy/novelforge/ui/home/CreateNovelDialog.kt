@file:OptIn(ExperimentalMaterial3Api::class)
package io.qzz.lstudy.novelforge.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.qzz.lstudy.novelforge.data.local.entity.Skill

/** 生成模式 */
enum class CreateMode {
    /** 逐章生成：创建后只生成第一章 */
    CHAPTER_BY_CHAPTER,
    /** 一次性生成全部：创建后自动生成所有目标章节 */
    ALL_AT_ONCE
}

/**
 * 创建小说对话框
 *
 * @param skills  可选 Skill 列表
 * @param providers  可选 AI 供应商列表（key + 显示名 + 内置模型 + 是否可调用）
 * @param customModels  各 provider 的自定义模型映射
 * @param defaultProvider  默认选中的 provider
 * @param onCreate  回调：(title, targetWords, targetChapters, provider, model, mode)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNovelDialog(
    skills: List<Skill>,
    providers: List<ProviderOption> = defaultProviderOptions(),
    customModels: Map<String, List<String>> = emptyMap(),
    defaultProvider: String = "deepseek",
    onDismiss: () -> Unit,
    onCreate: (title: String, targetWords: Int, targetChapters: Int, provider: String, model: String, mode: CreateMode) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetWords by remember { mutableStateOf("200000") }
    var targetChapters by remember { mutableStateOf("20") }
    var selectedSkill by remember { mutableStateOf<Skill?>(null) }
    var selectedProvider by remember { mutableStateOf(defaultProvider) }
    var selectedModel by remember { mutableStateOf("") }
    var createMode by remember { mutableStateOf(CreateMode.CHAPTER_BY_CHAPTER) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }

    val providerInfo = providers.firstOrNull { it.key == selectedProvider }
    val availableModels = (providerInfo?.models ?: emptyList()) +
        (customModels[selectedProvider] ?: emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新小说") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("小说标题") },
                    placeholder = { Text("请输入小说标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 目标字数
                OutlinedTextField(
                    value = targetWords,
                    onValueChange = { if (it.all(Char::isDigit)) targetWords = it },
                    label = { Text("目标字数") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 目标章节数
                OutlinedTextField(
                    value = targetChapters,
                    onValueChange = { if (it.all(Char::isDigit)) targetChapters = it },
                    label = { Text("目标章节数（用于"一次性生成全部"）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 生成模式选择
                Text("生成模式", style = MaterialTheme.typography.labelLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = createMode == CreateMode.CHAPTER_BY_CHAPTER,
                        onClick = { createMode = CreateMode.CHAPTER_BY_CHAPTER }
                    )
                    Text("逐章生成", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = createMode == CreateMode.ALL_AT_ONCE,
                        onClick = { createMode = CreateMode.ALL_AT_ONCE }
                    )
                    Text("一次性生成全部", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                // 供应商选择
                Text("AI 供应商", style = MaterialTheme.typography.labelLarge)
                Box {
                    OutlinedTextField(
                        value = providerInfo?.displayName ?: "请选择",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { providerMenuExpanded = true },
                        label = { Text("供应商") },
                        trailingIcon = { Text("▼") }
                    )
                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false }
                    ) {
                        providers.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(p.displayName)
                                        if (!p.callable) {
                                            Text(
                                                "暂不支持直接调用",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedProvider = p.key
                                    selectedModel = p.models.firstOrNull() ?: ""
                                    providerMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 模型选择
                Text("模型", style = MaterialTheme.typography.labelLarge)
                Box {
                    OutlinedTextField(
                        value = selectedModel.ifBlank { "请选择" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { modelMenuExpanded = true },
                        label = { Text("模型") },
                        trailingIcon = { Text("▼") }
                    )
                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false }
                    ) {
                        availableModels.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    selectedModel = m
                                    modelMenuExpanded = false
                                }
                            )
                        }
                        if (availableModels.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("（无可用模型，请到设置中添加）") },
                                onClick = { modelMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Skill 选择（可选）
                Text("创作风格（可选）", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(skills, key = { it.id }) { s ->
                        FilterChip(
                            selected = selectedSkill?.id == s.id,
                            onClick = {
                                selectedSkill = if (selectedSkill?.id == s.id) null else s
                            },
                            label = { Text(s.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val words = targetWords.toIntOrNull() ?: 200000
                    val chapters = targetChapters.toIntOrNull() ?: 20
                    val model = selectedModel.ifBlank {
                        providerInfo?.models?.firstOrNull() ?: ""
                    }
                    onCreate(title, words, chapters, selectedProvider, model, createMode)
                }
            ) { Text("创建并打开") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 供应商选项（用于 UI 展示） */
data class ProviderOption(
    val key: String,
    val displayName: String,
    val models: List<String>,
    val callable: Boolean
)

/** 默认供应商选项（从 ProviderConfigs 派生） */
fun defaultProviderOptions(): List<ProviderOption> =
    io.qzz.lstudy.novelforge.data.ai.ProviderConfigs.ALL.map { cfg ->
        ProviderOption(
            key = cfg.key,
            displayName = cfg.displayName,
            models = cfg.models,
            callable = cfg.openAiCompatible
        )
    } + ProviderOption(
        key = "custom",
        displayName = "自定义供应商",
        models = emptyList(),
        callable = true
    )
