package io.qzz.lstudy.novelforge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.qzz.lstudy.novelforge.data.local.entity.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * 章节 DAO，提供 chapters 表的增删改查
 */
@Dao
interface ChapterDao {

    /** 新增章节，返回新行的 rowId */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter): Long

    /** 批量插入章节 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Chapter>): List<Long>

    /** 更新章节 */
    @Update
    suspend fun update(chapter: Chapter)

    /** 删除章节 */
    @Delete
    suspend fun delete(chapter: Chapter)

    /** 按ID查询单个章节 */
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getById(chapterId: Long): Chapter?

    /** 观察一本小说下的所有章节，按 order 升序 */
    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY `order` ASC")
    fun observeByNovel(novelId: Long): Flow<List<Chapter>>

    /** 查询一本小说下的所有章节（一次性，非Flow），按 order 升序 */
    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY `order` ASC")
    suspend fun listByNovel(novelId: Long): List<Chapter>

    /** 更新章节正文 */
    @Query("UPDATE chapters SET content = :content WHERE id = :chapterId")
    suspend fun updateContent(chapterId: Long, content: String)

    /** 删除某本小说下的所有章节 */
    @Query("DELETE FROM chapters WHERE novelId = :novelId")
    suspend fun deleteByNovel(novelId: Long)

    /** 统计某本小说的章节数量 */
    @Query("SELECT COUNT(*) FROM chapters WHERE novelId = :novelId")
    suspend fun countByNovel(novelId: Long): Int
}
