package io.qzz.lstudy.novelforge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Skill 创作风格模板实体类
 * 对应数据库中的 skills 表，存储内置与自定义的写作风格提示词模板
 */
@Entity(
    tableName = "skills",
    indices = [Index(value = ["name"], unique = true)]
)
data class Skill(
    /** 主键ID，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 风格名称（如"悬疑"、"言情"） */
    val name: String,
    /** 风格描述，用于在UI上展示 */
    val description: String,
    /** 提示词模板，含占位符（如 {summary}、{chapterCount}），由 PromptBuilder 替换 */
    val promptTemplate: String
)
