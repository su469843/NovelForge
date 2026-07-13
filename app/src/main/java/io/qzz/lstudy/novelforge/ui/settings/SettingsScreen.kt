package io.qzz.lstudy.novelforge.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import io.qzz.lstudy.novelforge.ui.theme.ALL_THEMES

/** 设置页：API Key 管理 + 自定义供应商 + 导出模式 + 主题切换 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKeys: Map<String, String>,
    exportMode: String,
    currentTheme: String = "purple",
    customBaseUrl: String = "",
    customModel: String = "",
    customModels: Map<String, List<String>> = emptyMap(),
    onBack: () -> Unit = {},
    onSetApiKey: (provider: String, key: String) -> Unit = { _, _ -> },
    onSetExportMode: (mode: String) -> Unit = {},
    onSetTheme: (theme: String) -> Unit = {},
    onSetCustomBaseUrl: (String) -> Unit = {},
    onSetCustomModel: (String) -> Unit = {},
    onAddCustomModel: (provider: String, model: String) -> Unit = { _, _ -> },
    onRemoveCustomModel: (provider: String, model: String) -> Unit = { _, _ -> }
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
                    initial = info.initial,
                    callable = info.callable,
                    currentKey = apiKeys[info.key] ?: "",
                    onSave = { onSetApiKey(info.key, it) },
                    builtinModels = SettingsViewModel.PROVIDERS.firstOrNull { it.key == info.key }?.models
                        ?: emptyList(),
                    customModels = customModels[info.key] ?: emptyList(),
                    onAddCustomModel = { onAddCustomModel(info.key, it) },
                    onRemoveCustomModel = { onRemoveCustomModel(info.key, it) }
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
                    initial = info.initial,
                    callable = info.callable,
                    currentKey = apiKeys[info.key] ?: "",
                    onSave = { onSetApiKey(info.key, it) },
                    builtinModels = SettingsViewModel.PROVIDERS.firstOrNull { it.key == info.key }?.models
                        ?: emptyList(),
                    customModels = customModels[info.key] ?: emptyList(),
                    onAddCustomModel = { onAddCustomModel(info.key, it) },
                    onRemoveCustomModel = { onRemoveCustomModel(info.key, it) }
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()

            // 自定义供应商
            Text("自定义", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            CustomProviderSection(
                currentCustomKey = apiKeys["custom"] ?: "",
                currentBaseUrl = customBaseUrl,
                currentModel = customModel,
                onSaveKey = { onSetApiKey("custom", it) },
                onSaveBaseUrl = { onSetCustomBaseUrl(it) },
                onSaveModel = { onSetCustomModel(it) }
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

/**
 * 供应商圆形头像：显示首字母
 * 用于在 API Key 列表中标识不同供应商
 */
@Composable
fun ProviderAvatar(
    initial: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 自定义供应商区域
 * 用户需填写 API Key、BaseUrl、模型名三项才能正常调用
 */
@Composable
fun CustomProviderSection(
    currentCustomKey: String,
    currentBaseUrl: String,
    currentModel: String,
    onSaveKey: (String) -> Unit,
    onSaveBaseUrl: (String) -> Unit,
    onSaveModel: (String) -> Unit
) {
    var customKey by remember { mutableStateOf(currentCustomKey) }
    var baseUrl by remember { mutableStateOf(currentBaseUrl) }
    var model by remember { mutableStateOf(currentModel) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProviderAvatar(initial = "C")
            Spacer(Modifier.width(12.dp))
            Column {
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
            }
        }

        Spacer(Modifier.height(8.dp))

        // BaseUrl
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("请求地址 (BaseUrl)") },
            placeholder = { Text("例如：https://api.example.com/v1") },
            singleLine = true
        )
        Text(
            "请填写完整的 BaseUrl，应用会自动拼接 /chat/completions 路径",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(8.dp))

        // 模型名
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("模型名 (Model)") },
            placeholder = { Text("例如：gpt-3.5-turbo、deepseek-chat、qwen-plus") },
            singleLine = true
        )
        Text(
            "调用 AI 时使用的模型标识，请参考对应供应商的文档",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(8.dp))

        // API Key
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customKey,
                onValueChange = { customKey = it },
                modifier = Modifier.weight(1f),
                label = { Text("API Key") },
                placeholder = { Text("请输入自定义供应商的 API Key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                onSaveKey(customKey)
                onSaveBaseUrl(baseUrl)
                onSaveModel(model)
            }) {
                Text("保存")
            }
        }
    }
}

/**
 * 单个 API Key 输入行（含供应商圆形头像）
 * 在 API Key 输入行下方附带模型管理区块：内置模型只读展示 + 自定义模型增删
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApiKeyField(
    provider: String,
    label: String,
    initial: String,
    callable: Boolean,
    currentKey: String,
    onSave: (String) -> Unit,
    builtinModels: List<String> = emptyList(),
    customModels: List<String> = emptyList(),
    onAddCustomModel: (String) -> Unit = {},
    onRemoveCustomModel: (String) -> Unit = {}
) {
    var keyValue by remember(provider) { mutableStateOf(currentKey) }
    // 新模型名输入框状态
    var newModelName by remember(provider) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProviderAvatar(initial = initial)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$label API Key",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!callable) {
                    Text(
                        "本应用暂不支持直接调用，可仅保存 Key 备用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
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

        // 模型管理区块：内置模型只读 + 自定义模型增删
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "模型列表",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                // 内置模型（只读展示）
                if (builtinModels.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "内置模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        builtinModels.forEach { model ->
                            Text(
                                text = model,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 自定义模型（可删除）
                if (customModels.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "自定义模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        customModels.forEach { model ->
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                IconButton(
                                    onClick = { onRemoveCustomModel(model) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除模型 $model",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 添加自定义模型输入框 + 按钮
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newModelName,
                        onValueChange = { newModelName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入模型名添加到自定义") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val name = newModelName.trim()
                            if (name.isNotEmpty()) {
                                onAddCustomModel(name)
                                newModelName = ""
                            }
                        }
                    ) {
                        Text("添加")
                    }
                }
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
