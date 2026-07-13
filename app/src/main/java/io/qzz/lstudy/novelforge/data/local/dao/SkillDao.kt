package io.qzz.lstudy.novelforge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.qzz.lstudy.novelforge.data.local.entity.Skill
import kotlinx.coroutines.flow.Flow

/**
 * Skill DAO，提供 skills 表的增删改查
 */
@Dao
interface SkillDao {

    /** 新增 Skill，返回新行的 rowId */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: Skill): Long

    /** 批量插入 Skill，用于应用首次启动时初始化内置模板 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(skills: List<Skill>): List<Long>

    /** 更新 Skill */
    @Update
    suspend fun update(skill: Skill)

    /** 删除 Skill */
    @Delete
    suspend fun delete(skill: Skill)

    /** 按ID查询单个 Skill */
    @Query("SELECT * FROM skills WHERE id = :skillId")
    suspend fun getById(skillId: Long): Skill?

    /** 观察所有 Skill，按名称升序 */
    @Query("SELECT * FROM skills ORDER BY name ASC")
    fun observeAll(): Flow<List<Skill>>

    /** 一次性查询所有 Skill（非 Flow），按名称升序，用于 Skill 匹配器在内存中打分 */
    @Query("SELECT * FROM skills ORDER BY name ASC")
    suspend fun listAll(): List<Skill>

    /** 按名称精确查询（用于查重） */
    @Query("SELECT * FROM skills WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Skill?

    /** 按关键词模糊匹配名称或描述，用于 Skill 匹配器推荐 */
    @Query("SELECT * FROM skills WHERE name LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' ORDER BY name ASC")
    suspend fun search(keyword: String): List<Skill>

    /** 统计 Skill 总数，用于判断是否需要初始化内置数据 */
    @Query("SELECT COUNT(*) FROM skills")
    suspend fun count(): Int
}
