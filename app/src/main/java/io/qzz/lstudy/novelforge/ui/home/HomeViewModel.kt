package io.qzz.lstudy.novelforge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.data.local.entity.Skill
import io.qzz.lstudy.novelforge.data.repository.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 主页 ViewModel：管理小说列表与创建流程 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val novelRepository: NovelRepository
) : ViewModel() {

    /** 所有小说列表，按创建时间倒序 */
    val novels: StateFlow<List<Novel>> = novelRepository
        .observeAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 所有 Skill 模板列表 */
    val skills: StateFlow<List<Skill>> = novelRepository
        .observeAllSkills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _createdNovelId = MutableStateFlow<Long?>(null)
    /** 最近创建的小说 ID，创建成功后短暂置为非 null，UI 消费后自动重置 */
    val createdNovelId: StateFlow<Long?> = _createdNovelId.asStateFlow()

    /** 创建一部新小说，返回新 ID */
    suspend fun createNovel(
        title: String,
        targetWords: Int,
        targetChapters: Int = 0,
        model: String = ""
    ): Long {
        val id = novelRepository.createNovel(title, targetWords, targetChapters, model)
        _createdNovelId.value = id
        return id
    }

    /** 消费创建成功事件 */
    fun consumeCreatedNovel() {
        _createdNovelId.value = null
    }

    /** 删除小说 */
    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelRepository.deleteNovel(novel)
        }
    }
}