package io.qzz.lstudy.novelforge.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import io.qzz.lstudy.novelforge.ui.theme.ALL_THEMES

/** 设置页：API Key 管理 + 导出模式选择 + 主题切换 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKeys: Map<String, String>,
    exportMode: String,
    currentTheme: String = "purple",
    onBack: () -> Unit = {},
    onSetApiKey: (provider: String, key: String) -> Unit = { _, _ -> },
    onSetExportMode: (mode: String) -> Unit = {},
    onSetTheme: (theme: String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    Text(
                        "返回",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onBack() },
                        color = MaterialTheme.colorScheme.onPrimary,
                        softWrap = false
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // API Key 设置区域
            Text("API Key 设置", style = MaterialTheme.typography.titleMedium)

            // 国内供应商
            Text("国内", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            SettingsViewModel.PROVIDERS.filter { it.category == "国内" }.forEach { info ->
                ApiKeyField(
                    provider = info.key,
                    label = info.displayName,
                    currentKey = apiKeys[info.key] ?: "",
                    onSave = { onSetApiKey(info.key, it) }
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()

            // 国际供应商
            Text("国际", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            SettingsViewModel.PROVIDERS.filter { it.category == "国际" }.forEach { info ->
                ApiKeyField(
                    provider = info.key,
                    label = info.displayName,
                    currentKey = apiKeys[info.key] ?: "",
                    onSave = { onSetApiKey(info.key, it) }
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()

            // 自定义供应商
            Text("自定义", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            CustomProviderSection(
                currentCustomKey = apiKeys["custom"] ?: "",
                onSave = { onSetApiKey("custom", it) }
            )

            Spacer(Modifier.height(8.dp))

            // 主题切换区域
            Text("主题颜色", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ALL_THEMES.forEach { themeInfo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetTheme(themeInfo.key) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == themeInfo.key,
                                onClick = null // 由外层 Row 的 clickable 统一处理
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(themeInfo.displayName, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    themeInfo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 导出模式区域
            Text("默认导出模式", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExportModeRow(
                        label = "本地安全模式",
                        description = "通过离线插件生成，内容不出本机",
                        selected = exportMode == SettingRepository.EXPORT_MODE_LOCAL,
                        onClick = { onSetExportMode(SettingRepository.EXPORT_MODE_LOCAL) }
                    )
                    Spacer(Modifier.height(8.dp))
                    ExportModeRow(
                        label = "云端极速模式",
                        description = "调用云端 Worker 转换，速度快但需上传内容",
                        selected = exportMode == SettingRepository.EXPORT_MODE_CLOUD,
                        onClick = { onSetExportMode(SettingRepository.EXPORT_MODE_CLOUD) }
                    )
                }
            }
        }
    }
}

/** 自定义供应商：输入名称与 API Key */
@Composable
fun CustomProviderSection(
    currentCustomKey: String,
    onSave: (String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf(currentCustomKey) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "自定义供应商",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            "支持任何兼容 OpenAI API 格式的第三方服务，如本地 LLM、代理服务等",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customKey,
                onValueChange = { customKey = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("请输入自定义供应商的 API Key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSave(customKey) }) {
                Text("保存")
            }
        }
    }
}

/** 单个 API Key 输入行 */
@Composable
fun ApiKeyField(
    provider: String,
    label: String,
    currentKey: String,
    onSave: (String) -> Unit
) {
    var keyValue by remember(provider) { mutableStateOf(currentKey) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label API Key",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = keyValue,
                onValueChange = { keyValue = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("请输入 $label 的 API Key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSave(keyValue) }) {
                Text("保存")
            }
        }
    }
}

/** 导出模式选项行 */
@Composable
fun ExportModeRow(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}