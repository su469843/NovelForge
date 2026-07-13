package io.qzz.lstudy.novelforge.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 设置页 ViewModel：管理 API Key 与导出模式 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingRepository: SettingRepository
) : ViewModel() {

    /** 默认导出模式 */
    val exportMode: StateFlow<String> = settingRepository
        .observeExportMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingRepository.EXPORT_MODE_LOCAL)

    /** 当前选中的 AI provider */
    val activeProvider: StateFlow<String> = settingRepository
        .observeActiveProvider()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek")

    /** 全部 API Key */
    val apiKeys: StateFlow<Map<String, String>> = settingRepository
        .observeApiKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setApiKey(provider: String, key: String) {
        viewModelScope.launch { settingRepository.setApiKey(provider, key) }
    }

    fun setExportMode(mode: String) {
        viewModelScope.launch { settingRepository.setExportMode(mode) }
    }

    fun setActiveProvider(provider: String) {
        viewModelScope.launch { settingRepository.setActiveProvider(provider) }
    }

    companion object {
        /** 支持的 AI 模型列表 */
        val PROVIDERS = listOf(
            "deepseek" to "DeepSeek",
            "openai" to "OpenAI",
            "claude" to "Claude (Anthropic)",
            "minimax" to "MiniMax"
        )
    }
}