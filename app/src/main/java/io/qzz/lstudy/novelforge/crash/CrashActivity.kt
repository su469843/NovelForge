package io.qzz.lstudy.novelforge.crash

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 崩溃展示 Activity
 * 当应用发生未捕获异常时，由 [CrashHandler] 启动，
 * 展示错误详情并提供「复制」和「确定」按钮。
 */
class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val error = intent.getStringExtra(EXTRA_ERROR) ?: "未知错误"
        setContent {
            MaterialTheme {
                CrashScreen(error = error, onDismiss = { finishAffinity() })
            }
        }
    }

    companion object {
        const val EXTRA_ERROR = "crash_error"
    }
}

@Composable
fun CrashScreen(error: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "应用遇到问题",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "请将以下错误信息复制并发送给开发者",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // 错误详情卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    error,
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(Modifier.height(16.dp))

            // 按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(error))
                        Toast.makeText(
                            it.context,
                            "已复制到剪贴板",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("复制")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("确定")
                }
            }
        }
    }
}