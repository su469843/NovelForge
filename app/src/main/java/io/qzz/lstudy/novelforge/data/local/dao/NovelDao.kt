package io.qzz.lstudy.novelforge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import kotlinx.coroutines.flow.Flow

/**
 * 小说 DAO，提供 novels 表的增删改查
 */
@Dao
interface NovelDao {

    /** 新增小说，冲突时替换，返回新行的 rowId */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(novel: Novel): Long

    /** 批量插入小说 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(novels: List<Novel>): List<Long>

    /** 更新小说信息 */
    @Update
    suspend fun update(novel: Novel)

    /** 删除小说（关联的章节会因外键 CASCADE 一起删除） */
    @Delete
    suspend fun delete(novel: Novel)

    /** 按ID删除小说 */
    @Query("DELETE FROM novels WHERE id = :novelId")
    suspend fun deleteById(novelId: Long)

    /** 按ID查询单本小说 */
    @Query("SELECT * FROM novels WHERE id = :novelId")
    suspend fun getById(novelId: Long): Novel?

    /** 观察所有小说，按创建时间倒序 */
    @Query("SELECT * FROM novels ORDER BY createTime DESC")
    fun observeAll(): Flow<List<Novel>>

    /** 观察单本小说的变化 */
    @Query("SELECT * FROM novels WHERE id = :novelId")
    fun observeById(novelId: Long): Flow<Novel?>

    /** 更新当前字数 */
    @Query("UPDATE novels SET currentWords = :currentWords WHERE id = :novelId")
    suspend fun updateCurrentWords(novelId: Long, currentWords: Int)

    /** 累加 token 用量 */
    @Query("UPDATE novels SET totalTokens = totalTokens + :tokens WHERE id = :novelId")
    suspend fun addTokens(novelId: Long, tokens: Int)

    /** 更新目标章节数 */
    @Query("UPDATE novels SET targetChapters = :targetChapters WHERE id = :novelId")
    suspend fun updateTargetChapters(novelId: Long, targetChapters: Int)

    /** 更新使用的模型 */
    @Query("UPDATE novels SET model = :model WHERE id = :novelId")
    suspend fun updateModel(novelId: Long, model: String)
}
