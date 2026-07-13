package io.qzz.lstudy.novelforge.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节实体类
 * 对应数据库中的 chapters 表，与 Novel 形成一对多关系
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Novel::class,
            parentColumns = ["id"],
            childColumns = ["novelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["novelId"]), Index(value = ["novelId", "order"], unique = true)]
)
data class Chapter(
    /** 主键ID，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属小说ID */
    val novelId: Long,
    /** 章节标题 */
    val title: String,
    /** 章节正文内容 */
    val content: String = "",
    /** 章节序号，用于排序，同一本小说内唯一 */
    val order: Int
)
