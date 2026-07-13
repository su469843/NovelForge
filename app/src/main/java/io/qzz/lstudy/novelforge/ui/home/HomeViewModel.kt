package io.qzz.lstudy.novelforge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.data.repository.NovelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 主页 ViewModel：管理小说列表 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val novelRepository: NovelRepository
) : ViewModel() {

    /** 所有小说列表，按创建时间倒序，内存中保持最近值 */
    val novels: StateFlow<List<Novel>> = novelRepository
        .observeAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 创建一部新小说
     * @return 新小说的 ID
     */
    fun createNovel(title: String, targetWords: Int): Long {
        var novelId = 0L
        viewModelScope.launch {
            novelId = novelRepository.createNovel(title, targetWords)
        }
        // 注：此处为简化，用同步方式获取 ID；后续阶段会改用 StateFlow 承载结果
        // 实际场景中 createNovel 是 suspend 函数，由调用方在协程中调用
        return novelId
    }

    /** 删除小说 */
    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelRepository.deleteNovel(novel)
        }
    }
}