package io.qzz.lstudy.novelforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.qzz.lstudy.novelforge.crash.GlobalCrashHandler
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
 * 启动时：安装全局异常处理器 + 在后台协程填充内置 Skill 模板。
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
        // 安装全局异常捕获（必须在其他初始化之前）
        GlobalCrashHandler.install(this)
        // 在后台协程中确保内置 Skill 模板已填充
        appScope.launch {
            novelRepository.ensureDefaultSkills()
        }
    }
}
