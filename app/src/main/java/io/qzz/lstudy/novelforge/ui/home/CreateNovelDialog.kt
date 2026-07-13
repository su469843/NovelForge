package io.qzz.lstudy.novelforge.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.qzz.lstudy.novelforge.data.local.entity.Skill
import kotlinx.coroutines.launch

/** 新建小说对话框 */
@Composable
fun CreateNovelDialog(
    skills: List<Skill>,
    onDismiss: () -> Unit,
    onCreate: (title: String, targetWords: Int, selectedSkillId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetWords by remember { mutableIntStateOf(200000) }
    var chapterCount by remember { mutableIntStateOf(10) }
    var wordsPerChapter by remember { mutableIntStateOf(2000) }
    var selectedSkillId by remember { mutableStateOf<Long?>(null) }
    var useChapterMode by remember { mutableStateOf(false) } // false=总字数模式, true=章节数模式
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建小说") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 小说名称
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("小说名称") },
                    placeholder = { Text("请输入小说名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 创作风格（Skill 选择）
                Text("创作风格", style = MaterialTheme.typography.labelLarge)
                if (skills.isEmpty()) {
                    Text(
                        "暂无可用风格模板",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                // 使用 FlowRow 效果 —— 手动分行
                val rows = skills.chunked(3)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { skill ->
                            FilterChip(
                                selected = selectedSkillId == skill.id,
                                onClick = {
                                    selectedSkillId = if (selectedSkillId == skill.id) null else skill.id
                                },
                                label = { Text(skill.name, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }

                // 目标字数 / 章节数切换
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { useChapterMode = false }) {
                        Text(
                            "按总字数",
                            color = if (!useChapterMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { useChapterMode = true }) {
                        Text(
                            "按章节数",
                            color = if (useChapterMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (!useChapterMode) {
                    OutlinedTextField(
                        value = targetWords.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> targetWords = v } },
                        label = { Text("目标总字数") },
                        placeholder = { Text("例如：200000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = chapterCount.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> chapterCount = v } },
                        label = { Text("目标章节数") },
                        placeholder = { Text("例如：10") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = wordsPerChapter.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> wordsPerChapter = v } },
                        label = { Text("每章字数") },
                        placeholder = { Text("例如：2000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalTarget = if (useChapterMode) chapterCount * wordsPerChapter else targetWords
                        onCreate(title, finalTarget, selectedSkillId)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("开始构思")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}