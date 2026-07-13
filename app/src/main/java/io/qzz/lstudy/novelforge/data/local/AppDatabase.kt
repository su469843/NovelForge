package io.qzz.lstudy.novelforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.qzz.lstudy.novelforge.data.local.dao.ChapterDao
import io.qzz.lstudy.novelforge.data.local.dao.NovelDao
import io.qzz.lstudy.novelforge.data.local.dao.SkillDao
import io.qzz.lstudy.novelforge.data.local.entity.Chapter
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.data.local.entity.Skill

/**
 * 应用主数据库
 * 包含 novels、chapters、skills 三张表
 *
 * version = 1：初始版本
 * exportSchema = true：导出 schema JSON 以便后续做迁移验证
 */
@Database(
    entities = [Novel::class, Chapter::class, Skill::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    /** 小说表 DAO */
    abstract fun novelDao(): NovelDao

    /** 章节表 DAO */
    abstract fun chapterDao(): ChapterDao

    /** Skill 表 DAO */
    abstract fun skillDao(): SkillDao

    companion object {
        /** 数据库文件名 */
        const val DB_NAME = "novelforge.db"

        /**
         * 应用首次启动时内置的 Skill 创作风格模板
         * 模板中的占位符由 PromptBuilder 替换：
         *   {summary}        —— 用户输入的小说梗概
         *   {chapterCount}   —— 目标章节数
         *   {wordsPerChapter}—— 每章目标字数
         */
        val DEFAULT_SKILLS: List<Skill> = listOf(
            Skill(
                name = "悬疑推理",
                description = "层层反转、伏笔密布的悬疑推理风格，注重线索铺陈与逻辑解谜",
                promptTemplate = "你是一位擅长悬疑推理的小说家。请基于以下梗概创作：{summary}。" +
                    "要求：共 {chapterCount} 章，每章约 {wordsPerChapter} 字。" +
                    "注重埋设伏笔与反转，每章末尾留下悬念，人物动机需合理且可推导。"
            ),
            Skill(
                name = "都市言情",
                description = "细腻温暖的都市言情风格，注重情感描写与人物互动",
                promptTemplate = "你是一位擅长都市言情的小说家。请基于以下梗概创作：{summary}。" +
                    "要求：共 {chapterCount} 章，每章约 {wordsPerChapter} 字。" +
                    "注重情感线索的递进与人物心理刻画，对话要自然，避免狗血情节。"
            ),
            Skill(
                name = "玄幻修真",
                description = "宏大世界观的玄幻修真风格，注重境界体系与战斗描写",
                promptTemplate = "你是一位擅长玄幻修真的小说家。请基于以下梗概创作：{summary}。" +
                    "要求：共 {chapterCount} 章，每章约 {wordsPerChapter} 字。" +
                    "构建清晰的境界体系与功法设定，战斗场面要有画面感，主角成长节奏需合理。"
            ),
            Skill(
                name = "历史架空",
                description = "考究耐读的历史架空风格，注重时代氛围与权谋博弈",
                promptTemplate = "你是一位擅长历史架空的小说家。请基于以下梗概创作：{summary}。" +
                    "要求：共 {chapterCount} 章，每章约 {wordsPerChapter} 字。" +
                    "注重时代氛围的营造与权谋博弈，人物言行需符合时代背景，避免出戏的现代词汇。"
            ),
            Skill(
                name = "科幻未来",
                description = "想象力丰富的科幻未来风格，注重科技设定与人文思考",
                promptTemplate = "你是一位擅长科幻未来的小说家。请基于以下梗概创作：{summary}。" +
                    "要求：共 {chapterCount} 章，每章约 {wordsPerChapter} 字。" +
                    "构建自洽的科技设定，融入人文思考与伦理冲突，避免堆砌术语而忽视故事性。"
            )
        )
    }
}
