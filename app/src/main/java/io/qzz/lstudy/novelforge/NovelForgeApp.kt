package io.qzz.lstudy.novelforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.qzz.lstudy.novelforge.data.repository.NovelRepository
import io.qzz.lstudy.novelforge.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NovelForge 应用入口
 *
 * 标注 @HiltAndroidApp 后，Hilt 会自动生成依赖注入容器，
 * 并在 Application 创建时完成全局组件的初始化。
 *
 * 启动时在应用级协程中触发内置 Skill 模板的预填充，
 * 不阻塞主线程。
 */
@HiltAndroidApp
class NovelForgeApp : Application() {

    @Inject
    lateinit var novelRepository: NovelRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // 在后台协程中确保内置 Skill 模板已填充
        appScope.launch {
            novelRepository.ensureDefaultSkills()
        }
    }
}
