package io.qzz.lstudy.novelforge.crash

import android.app.Application
import android.content.Intent
import kotlin.system.exitProcess

/**
 * 全局异常捕获处理器
 *
 * 捕获所有未被处理的异常，展示错误弹窗让用户复制到剪贴板，
 * 方便开发者排查问题。
 */
class GlobalCrashHandler(
    private val application: Application,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 收集完整堆栈信息
        val stackTrace = StringBuilder()
        stackTrace.append("===== NovelForge Crash Log =====\n")
        stackTrace.append("Thread: ${thread.name}\n\n")
        var cause: Throwable? = throwable
        var depth = 0
        while (cause != null && depth < 20) {
            stackTrace.append("Caused by: ")
            stackTrace.append(cause.javaClass.name)
            stackTrace.append(": ")
            stackTrace.append(cause.message)
            stackTrace.append("\n")
            depth++
            cause = cause.cause
        }
        stackTrace.append("\nStack trace:\n")
        throwable.stackTrace.take(60).forEach { element ->
            stackTrace.append("    at $element\n")
        }
        if (throwable.stackTrace.size > 60) {
            stackTrace.append("    ... (truncated)\n")
        }
        stackTrace.append("===== End Log =====")

        // 启动 CrashActivity 展示错误
        val context = application.applicationContext
        val intent = Intent(context, CrashActivity::class.java).apply {
            putExtra(CrashActivity.EXTRA_ERROR, stackTrace.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)

        // 延迟退出，给 CrashActivity 时间启动
        try {
            Thread.sleep(1000)
        } catch (_: InterruptedException) { }

        // 退出当前进程
        exitProcess(1)
    }

    companion object {
        /**
         * 安装全局异常处理器
         */
        fun install(application: Application) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalCrashHandler(application, defaultHandler)
            )
        }
    }
}