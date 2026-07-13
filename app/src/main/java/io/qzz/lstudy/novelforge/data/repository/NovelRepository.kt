package io.qzz.lstudy.novelforge.data.repository

import io.qzz.lstudy.novelforge.data.local.AppDatabase
import io.qzz.lstudy.novelforge.data.local.dao.ChapterDao
import io.qzz.lstudy.novelforge.data.local.dao.NovelDao
import io.qzz.lstudy.novelforge.data.local.dao.SkillDao
import io.qzz.lstudy.novelforge.data.local.entity.Chapter
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.data.local.entity.Skill
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 小说数据仓库
 *
 * 封装 novels、chapters、skills 三张表的数据库操作，
 * 对上层屏蔽 DAO 细节，提供业务语义清晰的方法。
 *
 * 内置 Skill 模板的填充通过 [ensureDefaultSkills] 触发，
 * 由 Application 作用域协程在启动后调用一次。
 */
@Singleton
class NovelRepository @Inject constructor(
    private val novelDao: NovelDao,
    private val chapterDao: ChapterDao,
    private val skillDao: SkillDao
) {

    // ===================== Novel 操作 =====================

    /** 观察所有小说，按创建时间倒序 */
    fun observeAllNovels(): Flow<List<Novel>> = novelDao.observeAll()

    /** 观察单本小说 */
    fun observeNovel(novelId: Long): Flow<Novel?> = novelDao.observeById(novelId)

    /** 查询单本小说（一次性） */
    suspend fun getNovel(novelId: Long): Novel? = novelDao.getById(novelId)

    /** 创建一本新小说，返回新 ID */
    suspend fun createNovel(title: String, targetWords: Int): Long {
        val now = System.currentTimeMillis()
        return novelDao.insert(
            Novel(
                title = title,
                targetWords = targetWords,
                currentWords = 0,
                createTime = now
            )
        )
    }

    /** 更新小说信息 */
    suspend fun updateNovel(novel: Novel) = novelDao.update(novel)

    /** 删除小说（外键 CASCADE 会连带删除其下章节） */
    suspend fun deleteNovel(novel: Novel) = novelDao.delete(novel)

    /** 按 ID 删除小说 */
    suspend fun deleteNovelById(novelId: Long) = novelDao.deleteById(novelId)

    /** 更新小说当前字数 */
    suspend fun updateNovelCurrentWords(novelId: Long, currentWords: Int) =
        novelDao.updateCurrentWords(novelId, currentWords)

    // ===================== Chapter 操作 =====================

    /** 观察一本小说下的所有章节，按 order 升序 */
    fun observeChapters(novelId: Long): Flow<List<Chapter>> = chapterDao.observeByNovel(novelId)

    /** 一次性查询一本小说下的所有章节，按 order 升序 */
    suspend fun listChapters(novelId: Long): List<Chapter> = chapterDao.listByNovel(novelId)

    /** 查询单个章节 */
    suspend fun getChapter(chapterId: Long): Chapter? = chapterDao.getById(chapterId)

    /** 新增章节，返回新 ID */
    suspend fun addChapter(novelId: Long, title: String, order: Int, content: String = ""): Long =
        chapterDao.insert(
            Chapter(
                novelId = novelId,
                title = title,
                content = content,
                order = order
            )
        )

    /** 批量新增章节（例如 AI 生成大纲后一次性写入） */
    suspend fun addChapters(chapters: List<Chapter>): List<Long> = chapterDao.insertAll(chapters)

    /** 更新章节 */
    suspend fun updateChapter(chapter: Chapter) = chapterDao.update(chapter)

    /** 更新章节正文 */
    suspend fun updateChapterContent(chapterId: Long, content: String) =
        chapterDao.updateContent(chapterId, content)

    /** 删除章节 */
    suspend fun deleteChapter(chapter: Chapter) = chapterDao.delete(chapter)

    /** 统计一本小说的章节数 */
    suspend fun countChapters(novelId: Long): Int = chapterDao.countByNovel(novelId)

    // ===================== Skill 操作 =====================

    /** 观察所有 Skill 模板，按名称升序 */
    fun observeAllSkills(): Flow<List<Skill>> = skillDao.observeAll()

    /** 查询单个 Skill */
    suspend fun getSkill(skillId: Long): Skill? = skillDao.getById(skillId)

    /** 按名称查询 Skill */
    suspend fun getSkillByName(name: String): Skill? = skillDao.getByName(name)

    /**
     * 按关键词模糊搜索 Skill（用于 Skill 匹配器推荐）
     * 关键词会同时匹配 name 与 description
     */
    suspend fun searchSkills(keyword: String): List<Skill> = skillDao.search(keyword)

    /** 获取所有 Skill（一次性），用于 Skill 匹配器在内存中打分 */
    suspend fun listAllSkills(): List<Skill> = skillDao.listAll()

    /** 新增自定义 Skill，返回新 ID */
    suspend fun addSkill(skill: Skill): Long = skillDao.insert(skill)

    /** 更新 Skill */
    suspend fun updateSkill(skill: Skill) = skillDao.update(skill)

    /** 删除 Skill */
    suspend fun deleteSkill(skill: Skill) = skillDao.delete(skill)

    /**
     * 确保内置 Skill 模板已填充到数据库
     * 应用首次启动时由 Application 作用域协程调用，后续启动若 count > 0 则跳过
     */
    suspend fun ensureDefaultSkills() {
        if (skillDao.count() == 0) {
            skillDao.insertAll(AppDatabase.DEFAULT_SKILLS)
        }
    }
}
