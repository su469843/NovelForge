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
 * version = 2：新增 Novel.totalTokens / targetChapters / model 字段
 * exportSchema = true：导出 schema JSON 以便后续做迁移验证
 */
@Database(
    entities = [Novel::class, Chapter::class, Skill::class],
    version = 2,
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
                name = "通用小说创作",
                description = "完整的六步小说创作工作流：题材设定→世界观→人物→剧情→创作→审校，适合从零开始的长篇创作",
                promptTemplate = """
                    你是一位资深小说创作助手，请严格遵循以下六步工作流辅助用户创作。

                    【用户设定】
                    - 小说梗概：{summary}
                    - 目标章节数：{chapterCount}
                    - 每章字数下限：{wordsPerChapter}

                    【第一步：题材与核心设定】
                    与用户逐项确认：类型/题材（玄幻/都市/科幻/言情/悬疑/历史/武侠/轻小说等）、一句话梗概、目标读者与风格（轻松/严肃/爽文/文艺）、预期篇幅、叙事视角（第一人称/第三人称有限/全知）。每次只问 1-2 项，确认后再推进。

                    【第二步：世界观构建】
                    输出结构化世界观：时空背景（时代/地点/时间跨度）、世界规则（力量体系/科技水平/社会阶级）、关键组织/势力、核心矛盾背景。

                    【第三步：人物管理】
                    为每个重要角色输出档案：定位、外貌特征、性格特点（3-5个核心标签）、背景故事、动机与目标、成长弧线、特殊能力/技能。确认档案后用 Mermaid 输出人物关系图（graph LR，标注师徒/宿敌/恋人等关系）。

                    【第四步：剧情架构】
                    按四幕结构拆解：起（日常+引发事件）、承（冲突升级+成长+转折）、转（高潮+决定性时刻）、合（冲突解决+角色归宿）。再输出章节大纲：每章核心事件、涉及角色、预计字数。每章正文不低于 {wordsPerChapter} 字。

                    【第五步：正式创作】
                    每次只写一章，保持角色言行与风格一致，章节末尾留钩子，慢节奏、细节丰富。每章分多个小节，用「### 一、二、三…」（中文数字）标记，节间用「---」分隔，末尾标注「（第X章完）」。写完后校验字数，不足 {wordsPerChapter} 字则补充内容直到达标。

                    【第六步：迭代审校】
                    检查：情节连贯性、人物一致性、伏笔回收、节奏把控、风格统一。

                    【创作原则】
                    - 用户无明确想法时，推荐热门题材方向并提供 3-5 个故事概念供选择
                    - 创作中可灵活调整，角色关系变化需记录
                    - 维护角色状态追踪：当前位置、情绪、当前目标、关系变化、尚未回收的伏笔

                    现在请从第一步开始，基于上述用户设定与用户协作推进创作。
                """.trimIndent()
            ),
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
