package io.qzz.lstudy.novelforge.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.qzz.lstudy.novelforge.data.local.AppDatabase
import io.qzz.lstudy.novelforge.data.local.dao.ChapterDao
import io.qzz.lstudy.novelforge.data.local.dao.NovelDao
import io.qzz.lstudy.novelforge.data.local.dao.SkillDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 应用级协程作用域限定符
 * 用于标识应用全局生命周期内的协程作用域
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * 数据库与协程作用域的 Hilt 提供模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * v1 → v2 迁移：novels 表新增 totalTokens / targetChapters / model 字段
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE novels ADD COLUMN totalTokens INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE novels ADD COLUMN targetChapters INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE novels ADD COLUMN model TEXT NOT NULL DEFAULT ''")
        }
    }

    /**
     * 提供 AppDatabase 单例
     * 注意：内置 Skill 的初始化不在此处用 RoomDatabase.Callback 触发，
     * 因为 Callback 内无法注入 Repository。改由 Application 在启动时调用
     * NovelRepository.ensureDefaultSkills() 完成。
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideNovelDao(db: AppDatabase): NovelDao = db.novelDao()

    @Provides
    fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao()

    @Provides
    fun provideSkillDao(db: AppDatabase): SkillDao = db.skillDao()

    /**
     * 提供应用级协程作用域
     * 使用 SupervisorJob 保证子协程失败不会取消兄弟协程
     * 默认调度器为 Default，适合 CPU/DB 后台任务
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
