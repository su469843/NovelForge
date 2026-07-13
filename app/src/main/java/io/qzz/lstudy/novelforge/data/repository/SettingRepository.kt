package io.qzz.lstudy.novelforge.data.repository

import io.qzz.lstudy.novelforge.data.local.prefs.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户设置仓库
 *
 * 封装 [SettingsDataStore]，对外提供业务语义清晰的设置读写方法。
 * 上层（ViewModel）只依赖本接口，不直接接触 DataStore。
 */
@Singleton
class SettingRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {

    // ===================== API Key =====================

    /** 观察所有 provider 的 API Key */
    fun observeApiKeys(): Flow<Map<String, String>> = dataStore.observeApiKeys()

    /** 一次性获取所有 API Key */
    suspend fun getApiKeys(): Map<String, String> = dataStore.getApiKeys()

    /** 观察单个 provider 的 API Key */
    fun observeApiKey(provider: String): Flow<String?> = dataStore.observeApiKey(provider)

    /** 设置/更新（或清空）某个 provider 的 API Key */
    suspend fun setApiKey(provider: String, apiKey: String) =
        dataStore.setApiKey(provider, apiKey)

    // ===================== 默认导出模式 =====================

    /** 观察默认导出模式：[SettingsDataStore.DEFAULT_EXPORT_MODE] 或 "cloud" */
    fun observeExportMode(): Flow<String> = dataStore.observeExportMode()

    /** 设置默认导出模式 */
    suspend fun setExportMode(mode: String) = dataStore.setExportMode(mode)

    // ===================== 当前 AI provider =====================

    /** 观察当前选中的 AI provider */
    fun observeActiveProvider(): Flow<String> = dataStore.observeActiveProvider()

    /** 设置当前选中的 AI provider */
    suspend fun setActiveProvider(provider: String) = dataStore.setActiveProvider(provider)

    // ===================== 默认创作参数 =====================

    /** 观察默认目标章节数 */
    fun observeDefaultChapterCount(): Flow<Int> = dataStore.observeDefaultChapterCount()

    /** 设置默认目标章节数 */
    suspend fun setDefaultChapterCount(count: Int) = dataStore.setDefaultChapterCount(count)

    /** 观察默认每章字数 */
    fun observeDefaultWordsPerChapter(): Flow<Int> = dataStore.observeDefaultWordsPerChapter()

    /** 设置默认每章字数 */
    suspend fun setDefaultWordsPerChapter(words: Int) = dataStore.setDefaultWordsPerChapter(words)

    companion object {
        /** 本地导出模式标识 */
        const val EXPORT_MODE_LOCAL = SettingsDataStore.DEFAULT_EXPORT_MODE

        /** 云端导出模式标识 */
        const val EXPORT_MODE_CLOUD = "cloud"
    }
}
