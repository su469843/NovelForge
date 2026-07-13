package io.qzz.lstudy.novelforge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 小说实体类
 * 对应数据库中的 novels 表，存储一本小说的基本信息
 */
@Entity(
    tableName = "novels",
    indices = [Index(value = ["title"], unique = false)]
)
data class Novel(
    /** 主键ID，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 小说标题 */
    val title: String,
    /** 目标总字数 */
    val targetWords: Int,
    /** 当前已写字数 */
    val currentWords: Int = 0,
    /** 创建时间戳（毫秒） */
    val createTime: Long,
    /** 累计消耗的 token 总量（来自 AI 返回的 usage.total_tokens） */
    val totalTokens: Int = 0,
    /** 目标章节数（创建时设定，用于"一次性生成全部"模式） */
    val targetChapters: Int = 0,
    /** 使用的模型名（创建时选定） */
    val model: String = ""
)
